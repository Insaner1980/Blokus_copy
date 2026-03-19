package com.emmagame.polyomino.ai

data class AiState(
    val lastDirection: Pair<Int, Int> = 0 to 0,
    val frustration: Int = 0,
    val lastPlacedRow: Int = -1,
    val lastPlacedCol: Int = -1
)
