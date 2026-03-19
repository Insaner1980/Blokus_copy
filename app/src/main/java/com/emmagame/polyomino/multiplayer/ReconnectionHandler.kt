package com.emmagame.polyomino.multiplayer

import com.emmagame.polyomino.model.Difficulty
import com.emmagame.polyomino.model.GameConstants
import com.emmagame.polyomino.model.GameState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ReconnectionState(
    val isWaiting: Boolean = false,
    val disconnectedPlayerIndex: Int = -1,
    val remainingSeconds: Int = 0,
    val timedOut: Boolean = false
)

class ReconnectionHandler(
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(ReconnectionState())
    val state: StateFlow<ReconnectionState> = _state

    private var countdownJob: Job? = null

    fun onPlayerDisconnected(playerIndex: Int) {
        _state.value = ReconnectionState(
            isWaiting = true,
            disconnectedPlayerIndex = playerIndex,
            remainingSeconds = (GameConstants.RECONNECT_TIMEOUT_MS / 1000).toInt()
        )

        countdownJob?.cancel()
        countdownJob = scope.launch {
            var remaining = (GameConstants.RECONNECT_TIMEOUT_MS / 1000).toInt()
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _state.value = _state.value.copy(remainingSeconds = remaining)
            }
            if (isActive) {
                _state.value = _state.value.copy(
                    isWaiting = false,
                    timedOut = true
                )
            }
        }
    }

    fun onPlayerReconnected() {
        countdownJob?.cancel()
        _state.value = ReconnectionState()
    }

    fun reset() {
        countdownJob?.cancel()
        _state.value = ReconnectionState()
    }

    fun makeDisconnectedPlayerAi(state: GameState): GameState {
        val disconnectedIdx = _state.value.disconnectedPlayerIndex
        if (disconnectedIdx < 0) return state

        val newPlayers = state.players.toMutableList()
        val player = newPlayers[disconnectedIdx]
        newPlayers[disconnectedIdx] = player.copy(isAi = true, name = "${player.name} (AI)")

        reset()
        return state.copy(
            players = newPlayers,
            difficulty = Difficulty.MEDIUM
        )
    }
}
