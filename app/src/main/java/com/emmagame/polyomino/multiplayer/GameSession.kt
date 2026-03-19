package com.emmagame.polyomino.multiplayer

import com.emmagame.polyomino.model.GameMode
import com.emmagame.polyomino.model.GameState
import com.emmagame.polyomino.model.Move
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
data class SessionPlayer(
    val index: Int,
    val name: String,
    val isLocal: Boolean,
    val isAi: Boolean,
    val colorIndex: Int
)

enum class ConnectionState {
    DISCONNECTED, DISCOVERING, CONNECTING, CONNECTED, RECONNECTING, FAILED
}

enum class SessionType {
    LOCAL, BLUETOOTH, WIFI_DIRECT
}

interface GameSession {
    val sessionType: SessionType
    val gameMode: GameMode
    val players: StateFlow<List<SessionPlayer>>
    val incomingMoves: SharedFlow<Move>
    val connectionState: StateFlow<ConnectionState>

    suspend fun sendMove(move: Move): Result<Unit>
    suspend fun sendPass(playerIndex: Int): Result<Unit>
    suspend fun sendGameState(state: GameState): Result<Unit>
    suspend fun disconnect()
}
