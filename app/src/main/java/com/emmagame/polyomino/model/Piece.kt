package com.emmagame.polyomino.model

data class PieceDef(val name: String, val cells: List<Cell>)

object Pieces {
    val ALL: List<PieceDef> = listOf(
        // 1-cell (monomino)
        PieceDef("1", listOf(Cell(0, 0))),

        // 2-cell (domino)
        PieceDef("2", listOf(Cell(0, 0), Cell(0, 1))),

        // 3-cell (trominoes)
        PieceDef("I3", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2))),
        PieceDef("V3", listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0))),

        // 4-cell (tetrominoes)
        PieceDef("I4", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(0, 3))),
        PieceDef("O4", listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0), Cell(1, 1))),
        PieceDef("T4", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(1, 1))),
        PieceDef("L4", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(1, 0))),
        PieceDef("S4", listOf(Cell(0, 0), Cell(0, 1), Cell(1, 1), Cell(1, 2))),

        // 5-cell (pentominoes)
        PieceDef("F5", listOf(Cell(0, 1), Cell(0, 2), Cell(1, 0), Cell(1, 1), Cell(2, 1))),
        PieceDef("I5", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(0, 3), Cell(0, 4))),
        PieceDef("L5", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(0, 3), Cell(1, 0))),
        PieceDef("N5", listOf(Cell(0, 0), Cell(0, 1), Cell(1, 1), Cell(1, 2), Cell(1, 3))),
        PieceDef("P5", listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0), Cell(1, 1), Cell(2, 0))),
        PieceDef("T5", listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(1, 1), Cell(2, 1))),
        PieceDef("U5", listOf(Cell(0, 0), Cell(0, 2), Cell(1, 0), Cell(1, 1), Cell(1, 2))),
        PieceDef("V5", listOf(Cell(0, 0), Cell(1, 0), Cell(2, 0), Cell(2, 1), Cell(2, 2))),
        PieceDef("W5", listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1), Cell(2, 1), Cell(2, 2))),
        PieceDef("X5", listOf(Cell(0, 1), Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(2, 1))),
        PieceDef("Y5", listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1), Cell(2, 0), Cell(3, 0))),
        PieceDef("Z5", listOf(Cell(0, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(2, 2))),
    )

    init {
        require(ALL.size == GameConstants.PIECE_COUNT) {
            "Expected ${GameConstants.PIECE_COUNT} pieces, got ${ALL.size}"
        }
        val totalCells = ALL.sumOf { it.cells.size }
        require(totalCells == GameConstants.TOTAL_CELLS) {
            "Expected ${GameConstants.TOTAL_CELLS} total cells, got $totalCells"
        }
    }
}
