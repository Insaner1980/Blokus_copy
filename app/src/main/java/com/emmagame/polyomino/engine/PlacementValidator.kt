package com.emmagame.polyomino.engine

import com.emmagame.polyomino.model.Board
import com.emmagame.polyomino.model.Cell
import com.emmagame.polyomino.model.GameState

object PlacementValidator {

    private val ORTHO_OFFSETS = listOf(Cell(-1, 0), Cell(1, 0), Cell(0, -1), Cell(0, 1))
    private val DIAG_OFFSETS = listOf(Cell(-1, -1), Cell(-1, 1), Cell(1, -1), Cell(1, 1))

    fun isValidPlacement(
        state: GameState,
        playerIndex: Int,
        cells: List<Cell>
    ): Boolean {
        val board = state.board
        val player = state.players[playerIndex]
        val isFirstPiece = player.piecesPlaced == 0

        // 1. All cells within bounds
        if (cells.any { !board.inBounds(it.row, it.col) }) return false

        // 2. All cells land on empty squares
        if (cells.any { board.get(it.row, it.col) != Board.EMPTY }) return false

        val cellSet = cells.toSet()

        // 3. No orthogonal adjacency with same player's existing pieces
        for (cell in cells) {
            for (offset in ORTHO_OFFSETS) {
                val nr = cell.row + offset.row
                val nc = cell.col + offset.col
                if (board.inBounds(nr, nc) &&
                    Cell(nr, nc) !in cellSet &&
                    board.get(nr, nc) == playerIndex
                ) {
                    return false
                }
            }
        }

        // 4. First piece must cover assigned start position
        if (isFirstPiece) {
            val startPos = state.startPositions[playerIndex]
            return cells.any { it == startPos }
        }

        // 5. Subsequent pieces must touch at least one diagonal neighbor of same player
        for (cell in cells) {
            for (offset in DIAG_OFFSETS) {
                val nr = cell.row + offset.row
                val nc = cell.col + offset.col
                if (board.inBounds(nr, nc) &&
                    Cell(nr, nc) !in cellSet &&
                    board.get(nr, nc) == playerIndex
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun getCornerPositions(state: GameState, playerIndex: Int): Set<Cell> {
        val board = state.board
        val player = state.players[playerIndex]

        if (player.piecesPlaced == 0) {
            val startPos = state.startPositions[playerIndex]
            return if (board.get(startPos.row, startPos.col) == Board.EMPTY) {
                setOf(startPos)
            } else {
                emptySet()
            }
        }

        val corners = mutableSetOf<Cell>()
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                if (board.get(row, col) != Board.EMPTY) continue

                // Check not orthogonally adjacent to same player
                val orthoAdjacent = ORTHO_OFFSETS.any { offset ->
                    val nr = row + offset.row
                    val nc = col + offset.col
                    board.inBounds(nr, nc) && board.get(nr, nc) == playerIndex
                }
                if (orthoAdjacent) continue

                // Check diagonally adjacent to same player
                val diagAdjacent = DIAG_OFFSETS.any { offset ->
                    val nr = row + offset.row
                    val nc = col + offset.col
                    board.inBounds(nr, nc) && board.get(nr, nc) == playerIndex
                }
                if (diagAdjacent) corners.add(Cell(row, col))
            }
        }
        return corners
    }
}
