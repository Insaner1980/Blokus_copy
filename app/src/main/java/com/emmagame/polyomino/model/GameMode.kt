package com.emmagame.polyomino.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode(
    val boardSize: Int,
    val playerCount: Int,
    val displayName: String
) {
    SOLO(GameConstants.STANDARD_BOARD_SIZE, 4, "Solo vs AI"),
    TWO_PLAYER_STANDARD(GameConstants.STANDARD_BOARD_SIZE, 4, "Two Player (20×20)"),
    TWO_PLAYER_DUO(GameConstants.DUO_BOARD_SIZE, 2, "Duo (14×14)"),
    THREE_PLAYER(GameConstants.STANDARD_BOARD_SIZE, 4, "Three Player"),
    FOUR_PLAYER(GameConstants.STANDARD_BOARD_SIZE, 4, "Four Player");

    fun getStartPositions(): List<Cell> = when (this) {
        TWO_PLAYER_DUO -> GameConstants.DUO_START_POSITIONS
        else -> GameConstants.STANDARD_CORNERS
    }
}
