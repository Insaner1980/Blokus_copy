package com.emmagame.polyomino.multiplayer

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.emmagame.polyomino.model.GameConstants
import com.emmagame.polyomino.model.GameMode
import com.emmagame.polyomino.model.GameState
import com.emmagame.polyomino.model.Move
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NearbySession(
    private val context: Context,
    override val gameMode: GameMode,
    private val isHost: Boolean,
    private val localPlayerName: String,
    private val scope: CoroutineScope
) : GameSession {

    override val sessionType = SessionType.BLUETOOTH // Nearby handles both BT and WiFi

    private val json = Json { ignoreUnknownKeys = true }
    private val client: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val serviceId = "com.emmagame.polyomino"

    private val connectedEndpoints = mutableMapOf<String, SessionPlayer>()
    private var nextPlayerIndex = if (isHost) 1 else 0

    private val _players = MutableStateFlow<List<SessionPlayer>>(
        if (isHost) listOf(
            SessionPlayer(0, localPlayerName, isLocal = true, isAi = false, colorIndex = 0)
        ) else emptyList()
    )
    override val players: StateFlow<List<SessionPlayer>> = _players

    private val _incomingMoves = MutableSharedFlow<Move>(extraBufferCapacity = 16)
    override val incomingMoves: SharedFlow<Move> = _incomingMoves

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _incomingMessages = MutableSharedFlow<GameMessage>(extraBufferCapacity = 16)
    val incomingMessages: SharedFlow<GameMessage> = _incomingMessages

    private var reconnectJob: Job? = null

    // --- Connection Lifecycle ---

    private val connectionCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Auto-accept all connections
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    val playerIdx = nextPlayerIndex++
                    val player = SessionPlayer(
                        index = playerIdx,
                        name = "Player ${playerIdx + 1}",
                        isLocal = false,
                        isAi = false,
                        colorIndex = playerIdx
                    )
                    connectedEndpoints[endpointId] = player
                    _players.value = buildPlayerList()
                    _connectionState.value = ConnectionState.CONNECTED
                    reconnectJob?.cancel()
                }
                else -> {
                    _connectionState.value = ConnectionState.FAILED
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            val player = connectedEndpoints.remove(endpointId)
            if (player != null) {
                _players.value = buildPlayerList()
                _incomingMessages.tryEmit(GameMessage.PlayerLeft(player.index))
                startReconnectTimer(endpointId)
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes() ?: return
                val messageStr = String(bytes)
                try {
                    val message = json.decodeFromString<GameMessage>(messageStr)
                    scope.launch {
                        handleMessage(endpointId, message)
                    }
                } catch (_: Exception) { }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // No-op for BYTES payloads (transfer is immediate)
        }
    }

    // --- Discovery ---

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // Guest: request connection to the found host
            _connectionState.value = ConnectionState.CONNECTING
            client.requestConnection(localPlayerName, endpointId, connectionCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            // Endpoint no longer visible
        }
    }

    // --- Public API ---

    fun startAdvertising() {
        _connectionState.value = ConnectionState.DISCOVERING
        val options = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_STAR)
            .build()
        client.startAdvertising(
            localPlayerName, serviceId, connectionCallback, options
        ).addOnFailureListener {
            _connectionState.value = ConnectionState.FAILED
        }
    }

    fun startDiscovery() {
        _connectionState.value = ConnectionState.DISCOVERING
        val options = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_STAR)
            .build()
        client.startDiscovery(serviceId, discoveryCallback, options)
            .addOnFailureListener {
                _connectionState.value = ConnectionState.FAILED
            }
    }

    fun stopDiscovery() {
        client.stopDiscovery()
        client.stopAdvertising()
    }

    override suspend fun sendMove(move: Move): Result<Unit> {
        val message = GameMessage.PlaceMove(move)
        return broadcastMessage(message)
    }

    override suspend fun sendPass(playerIndex: Int): Result<Unit> {
        val message = GameMessage.Pass(playerIndex)
        return broadcastMessage(message)
    }

    override suspend fun sendGameState(state: GameState): Result<Unit> {
        val message = GameMessage.FullSync(state)
        return broadcastMessage(message)
    }

    override suspend fun disconnect() {
        reconnectJob?.cancel()
        client.stopAllEndpoints()
        client.stopAdvertising()
        client.stopDiscovery()
        connectedEndpoints.clear()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun sendMessageToEndpoint(endpointId: String, message: GameMessage) {
        val bytes = json.encodeToString(message).toByteArray()
        client.sendPayload(endpointId, Payload.fromBytes(bytes))
    }

    // --- Internal ---

    private fun broadcastMessage(message: GameMessage): Result<Unit> {
        return try {
            val bytes = json.encodeToString(message).toByteArray()
            val payload = Payload.fromBytes(bytes)
            for (endpointId in connectedEndpoints.keys) {
                client.sendPayload(endpointId, payload)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleMessage(endpointId: String, message: GameMessage) {
        when (message) {
            is GameMessage.PlaceMove -> _incomingMoves.emit(message.move)
            is GameMessage.Pass -> _incomingMessages.emit(message)
            is GameMessage.FullSync -> _incomingMessages.emit(message)
            is GameMessage.PlayerJoined -> {
                val player = message.player
                connectedEndpoints[endpointId] = player
                _players.value = buildPlayerList()
            }
            is GameMessage.PlayerLeft -> _incomingMessages.emit(message)
            is GameMessage.GameConfig -> _incomingMessages.emit(message)
            is GameMessage.Ping -> {
                sendMessageToEndpoint(endpointId, GameMessage.Pong)
            }
            is GameMessage.Pong -> { /* keepalive response */ }
        }
    }

    private fun buildPlayerList(): List<SessionPlayer> {
        val local = if (isHost) {
            listOf(SessionPlayer(0, localPlayerName, isLocal = true, isAi = false, colorIndex = 0))
        } else emptyList()
        return local + connectedEndpoints.values.sortedBy { it.index }
    }

    private fun startReconnectTimer(endpointId: String) {
        _connectionState.value = ConnectionState.RECONNECTING
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(GameConstants.RECONNECT_TIMEOUT_MS)
            // Timeout expired — player doesn't reconnect
            _connectionState.value = ConnectionState.CONNECTED
            // The ViewModel will handle AI takeover for the disconnected player
        }
    }

    fun getConnectedEndpointCount(): Int = connectedEndpoints.size
}
