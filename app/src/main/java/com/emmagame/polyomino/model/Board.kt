package com.emmagame.polyomino.model

import kotlinx.serialization.Serializable

@Serializable
data class Board(
    val size: Int,
    val cells: IntArray
) {
    init {
        require(cells.size == size * size) {
            "Board cells size ${cells.size} doesn't match ${size}x${size}"
        }
    }

    companion object {
        const val EMPTY = -1

        fun create(size: Int): Board =
            Board(size, IntArray(size * size) { EMPTY })
    }

    fun index(row: Int, col: Int): Int = row * size + col
    fun row(idx: Int): Int = idx / size
    fun col(idx: Int): Int = idx % size

    fun get(row: Int, col: Int): Int = cells[index(row, col)]
    fun inBounds(row: Int, col: Int): Boolean =
        row in 0 until size && col in 0 until size

    fun set(row: Int, col: Int, value: Int): Board {
        val newCells = cells.copyOf()
        newCells[index(row, col)] = value
        return copy(cells = newCells)
    }

    fun withPlacements(placements: List<Cell>, playerIndex: Int): Board {
        val newCells = cells.copyOf()
        for (cell in placements) {
            newCells[index(cell.row, cell.col)] = playerIndex
        }
        return copy(cells = newCells)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Board) return false
        return size == other.size && cells.contentEquals(other.cells)
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + cells.contentHashCode()
        return result
    }
}
