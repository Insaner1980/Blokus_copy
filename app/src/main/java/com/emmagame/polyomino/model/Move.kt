package com.emmagame.polyomino.model

import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val playerIndex: Int,
    val pieceIndex: Int,
    val cells: List<Cell>,
    val rotations: Int = 0,
    val flipped: Boolean = false
)
