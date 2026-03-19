package com.emmagame.polyomino.model

object GameConstants {
    const val STANDARD_BOARD_SIZE = 20
    const val DUO_BOARD_SIZE = 14
    const val PLAYER_COUNT = 4
    const val PIECE_COUNT = 21
    const val TOTAL_CELLS = 89
    const val MAX_HISTORY_ENTRIES = 50
    const val DIFFICULTY_LEVELS = 5

    const val BOARD_INTERACTION_LOCK_MS = 160L
    const val INVALID_FEEDBACK_COOLDOWN_MS = 180L
    const val AI_TURN_DELAY_MIN_MS = 300L
    const val AI_TURN_DELAY_RANGE_MS = 400L
    const val TURN_ADVANCE_DELAY_MS = 400L
    const val HUMAN_AUTO_PASS_DELAY_MS = 1500L
    const val SAVE_NOTIFICATION_DURATION_MS = 2000L
    const val RECONNECT_TIMEOUT_MS = 60_000L
    const val BACKGROUND_TIMEOUT_MS = 300_000L

    const val MAX_AVATAR_DIMENSION = 160
    const val MAX_AVATAR_FILE_SIZE = 5 * 1024 * 1024

    val AVATAR_STYLES = listOf("bottts", "avataaars", "pixel-art", "fun-emoji", "lorelei", "thumbs")

    val STANDARD_CORNERS = listOf(
        Cell(0, 0), Cell(0, 19), Cell(19, 0), Cell(19, 19)
    )
    val DUO_START_POSITIONS = listOf(Cell(4, 4), Cell(9, 9))

    val PLAYER_NAMES = listOf("Player", "Amber", "Coral", "Teal")
    val PLAYER_COLORS = listOf("Indigo", "Amber", "Coral", "Teal")
}
