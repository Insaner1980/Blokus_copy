package com.emmagame.polyomino.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val board: Board,
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val gameMode: GameMode,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val moveHistory: List<Move> = emptyList(),
    val consecutivePasses: Int = 0,
    val isGameOver: Boolean = false,
    val startPositions: List<Cell> = emptyList(),
    val elapsedTimeSeconds: Int = 0,
    val sharedColorIndex: Int = -1,
    val sharedColorTurnCounter: Int = 0,
    val isPassAndPlay: Boolean = false
) {
    fun getControllingHumanIndex(playerIndex: Int): Int {
        val player = players[playerIndex]
        if (player.isSharedColor) {
            val realPlayers = players.filter { !it.isSharedColor && !it.isAi }
            return if (realPlayers.isNotEmpty()) {
                realPlayers[sharedColorTurnCounter % realPlayers.size].index
            } else -1
        }
        return if (player.controlledBy >= 0) player.controlledBy else player.index
    }
}
