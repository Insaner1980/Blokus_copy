package com.emmagame.polyomino.multiplayer

import com.emmagame.polyomino.model.GameConstants
import com.emmagame.polyomino.model.GameMode
import com.emmagame.polyomino.model.GameState
import com.emmagame.polyomino.model.Move
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class LocalSession(
    override val gameMode: GameMode
) : GameSession {

    override val sessionType = SessionType.LOCAL

    private val _players = MutableStateFlow(
        List(gameMode.playerCount) { i ->
            SessionPlayer(
                index = i,
                name = if (i < GameConstants.PLAYER_NAMES.size) GameConstants.PLAYER_NAMES[i] else "Player ${i + 1}",
                isLocal = true,
                isAi = gameMode == GameMode.SOLO && i != 0,
                colorIndex = i
            )
        }
    )
    override val players: StateFlow<List<SessionPlayer>> = _players

    private val _incomingMoves = MutableSharedFlow<Move>()
    override val incomingMoves: SharedFlow<Move> = _incomingMoves

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override suspend fun sendMove(move: Move): Result<Unit> = Result.success(Unit)

    override suspend fun sendPass(playerIndex: Int): Result<Unit> = Result.success(Unit)

    override suspend fun sendGameState(state: GameState): Result<Unit> = Result.success(Unit)

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
