package com.emmagame.polyomino.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val index: Int,
    val name: String,
    val usedPieces: BooleanArray = BooleanArray(GameConstants.PIECE_COUNT),
    val piecesPlaced: Int = 0,
    val score: Int = GameConstants.TOTAL_CELLS,
    val passed: Boolean = false,
    val isAi: Boolean = false,
    val isLocal: Boolean = true,
    val controlledBy: Int = -1,
    val isSharedColor: Boolean = false
) {
    fun withPiecePlaced(pieceIndex: Int, cellCount: Int): Player {
        val newUsed = usedPieces.copyOf()
        newUsed[pieceIndex] = true
        return copy(
            usedPieces = newUsed,
            piecesPlaced = piecesPlaced + 1,
            score = score - cellCount
        )
    }

    fun withPassed(): Player = copy(passed = true)

    val isHumanControlled: Boolean
        get() = !isAi && !isSharedColor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Player) return false
        return index == other.index && name == other.name &&
            usedPieces.contentEquals(other.usedPieces) &&
            piecesPlaced == other.piecesPlaced && score == other.score &&
            passed == other.passed && isAi == other.isAi && isLocal == other.isLocal &&
            controlledBy == other.controlledBy && isSharedColor == other.isSharedColor
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + name.hashCode()
        result = 31 * result + usedPieces.contentHashCode()
        result = 31 * result + piecesPlaced
        result = 31 * result + score
        result = 31 * result + passed.hashCode()
        result = 31 * result + isAi.hashCode()
        result = 31 * result + isLocal.hashCode()
        result = 31 * result + controlledBy
        result = 31 * result + isSharedColor.hashCode()
        return result
    }
}
