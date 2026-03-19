package com.emmagame.polyomino.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emmagame.polyomino.engine.GameEngine
import com.emmagame.polyomino.model.*
import com.emmagame.polyomino.multiplayer.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LobbyUiState(
    val isActive: Boolean = false,
    val isHost: Boolean = false,
    val gameMode: GameMode = GameMode.FOUR_PLAYER,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val players: List<SessionPlayer> = emptyList(),
    val gameStarted: Boolean = false,
    val initialGameState: GameState? = null
)

class MultiplayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _lobbyState = MutableStateFlow(LobbyUiState())
    val lobbyState: StateFlow<LobbyUiState> = _lobbyState.asStateFlow()

    private var nearbySession: NearbySession? = null
    private val reconnectionHandler = ReconnectionHandler(viewModelScope)

    val reconnectionState = reconnectionHandler.state

    fun hostGame(gameMode: GameMode, difficulty: Difficulty, playerName: String) {
        val session = NearbySession(
            context = getApplication(),
            gameMode = gameMode,
            isHost = true,
            localPlayerName = playerName,
            scope = viewModelScope
        )
        nearbySession = session

        _lobbyState.value = LobbyUiState(
            isActive = true,
            isHost = true,
            gameMode = gameMode,
            difficulty = difficulty
        )

        // Observe session state
        viewModelScope.launch {
            session.connectionState.collect { state ->
                _lobbyState.value = _lobbyState.value.copy(connectionState = state)
            }
        }
        viewModelScope.launch {
            session.players.collect { players ->
                _lobbyState.value = _lobbyState.value.copy(players = players)
            }
        }
        viewModelScope.launch {
            session.incomingMessages.collect { message ->
                handleIncomingMessage(message)
            }
        }

        session.startAdvertising()
    }

    fun joinGame(gameMode: GameMode, difficulty: Difficulty, playerName: String) {
        val session = NearbySession(
            context = getApplication(),
            gameMode = gameMode,
            isHost = false,
            localPlayerName = playerName,
            scope = viewModelScope
        )
        nearbySession = session

        _lobbyState.value = LobbyUiState(
            isActive = true,
            isHost = false,
            gameMode = gameMode,
            difficulty = difficulty
        )

        viewModelScope.launch {
            session.connectionState.collect { state ->
                _lobbyState.value = _lobbyState.value.copy(connectionState = state)
            }
        }
        viewModelScope.launch {
            session.players.collect { players ->
                _lobbyState.value = _lobbyState.value.copy(players = players)
            }
        }
        viewModelScope.launch {
            session.incomingMessages.collect { message ->
                handleIncomingMessage(message)
            }
        }

        session.startDiscovery()
    }

    fun startMultiplayerGame() {
        val lobby = _lobbyState.value
        if (!lobby.isHost) return

        val state = GameEngine.createInitialState(
            gameMode = lobby.gameMode,
            difficulty = lobby.difficulty,
            isPassAndPlay = false
        )

        // Send game state to all connected players
        viewModelScope.launch {
            nearbySession?.sendGameState(state)
        }

        _lobbyState.value = lobby.copy(
            gameStarted = true,
            initialGameState = state
        )
    }

    fun sendMove(move: Move) {
        viewModelScope.launch {
            nearbySession?.sendMove(move)
        }
    }

    fun sendPass(playerIndex: Int) {
        viewModelScope.launch {
            nearbySession?.sendPass(playerIndex)
        }
    }

    fun sendFullSync(state: GameState) {
        viewModelScope.launch {
            nearbySession?.sendGameState(state)
        }
    }

    fun leaveLobby() {
        viewModelScope.launch {
            nearbySession?.disconnect()
            nearbySession = null
            reconnectionHandler.reset()
            _lobbyState.value = LobbyUiState()
        }
    }

    fun handleReconnectTimeout(currentGameState: GameState): GameState {
        return reconnectionHandler.makeDisconnectedPlayerAi(currentGameState)
    }

    private fun handleIncomingMessage(message: GameMessage) {
        when (message) {
            is GameMessage.FullSync -> {
                _lobbyState.value = _lobbyState.value.copy(
                    gameStarted = true,
                    initialGameState = message.state
                )
            }
            is GameMessage.PlayerLeft -> {
                reconnectionHandler.onPlayerDisconnected(message.playerIndex)
            }
            is GameMessage.GameConfig -> {
                _lobbyState.value = _lobbyState.value.copy(
                    gameMode = message.mode,
                    difficulty = Difficulty.entries.firstOrNull { it.level == message.difficulty }
                        ?: Difficulty.MEDIUM
                )
            }
            else -> { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            nearbySession?.disconnect()
        }
        reconnectionHandler.reset()
    }
}
