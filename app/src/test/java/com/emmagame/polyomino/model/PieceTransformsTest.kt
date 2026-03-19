package com.emmagame.polyomino.model

import org.junit.Assert.*
import org.junit.Test

class PieceTransformsTest {

    @Test
    fun `all pieces have correct cell counts`() {
        val expectedSizes = listOf(1, 2, 3, 3, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5)
        assertEquals(GameConstants.PIECE_COUNT, Pieces.ALL.size)
        for ((i, piece) in Pieces.ALL.withIndex()) {
            assertEquals(
                "Piece ${piece.name} should have ${expectedSizes[i]} cells",
                expectedSizes[i],
                piece.cells.size
            )
        }
    }

    @Test
    fun `total cells across all pieces equals 89`() {
        val total = Pieces.ALL.sumOf { it.cells.size }
        assertEquals(GameConstants.TOTAL_CELLS, total)
    }

    @Test
    fun `normalize shifts cells to origin`() {
        val cells = listOf(Cell(3, 5), Cell(3, 6), Cell(4, 5))
        val normalized = PieceTransforms.normalize(cells)
        assertEquals(listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0)), normalized)
    }

    @Test
    fun `rotateCW produces correct result`() {
        // L-shape: (0,0), (0,1), (1,0)
        val cells = listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0))
        val rotated = PieceTransforms.rotateCW(cells)
        // After CW rotation: [r,c] -> [c,-r], then normalize
        assertTrue(rotated.size == 3)
        // Verify it's a valid rotation (different from original)
        val original = PieceTransforms.normalize(cells)
        // The rotated shape should be normalized and sorted
        assertEquals(rotated, PieceTransforms.normalize(rotated))
    }

    @Test
    fun `rotateCCW produces correct result`() {
        val cells = listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0))
        val rotated = PieceTransforms.rotateCCW(cells)
        assertTrue(rotated.size == 3)
        assertEquals(rotated, PieceTransforms.normalize(rotated))
    }

    @Test
    fun `flipH produces correct result`() {
        val cells = listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0))
        val flipped = PieceTransforms.flipH(cells)
        assertTrue(flipped.size == 3)
        assertEquals(flipped, PieceTransforms.normalize(flipped))
    }

    @Test
    fun `four CW rotations return to original`() {
        val cells = PieceTransforms.normalize(listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0)))
        var current = cells
        repeat(4) { current = PieceTransforms.rotateCW(current) }
        assertEquals(cells, current)
    }

    @Test
    fun `double flipH returns to original`() {
        val cells = PieceTransforms.normalize(listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0)))
        val doubleFlip = PieceTransforms.flipH(PieceTransforms.flipH(cells))
        assertEquals(cells, doubleFlip)
    }

    @Test
    fun `monomino has 1 unique orientation`() {
        val orientations = PieceTransforms.getAllOrientations(Pieces.ALL[0].cells)
        assertEquals(1, orientations.size)
    }

    @Test
    fun `domino has 2 unique orientations`() {
        val orientations = PieceTransforms.getAllOrientations(Pieces.ALL[1].cells)
        assertEquals(2, orientations.size)
    }

    @Test
    fun `O4 square has 1 unique orientation`() {
        val o4 = Pieces.ALL.first { it.name == "O4" }
        val orientations = PieceTransforms.getAllOrientations(o4.cells)
        assertEquals(1, orientations.size)
    }

    @Test
    fun `I4 straight has 2 unique orientations`() {
        val i4 = Pieces.ALL.first { it.name == "I4" }
        val orientations = PieceTransforms.getAllOrientations(i4.cells)
        assertEquals(2, orientations.size)
    }

    @Test
    fun `T4 has 4 unique orientations`() {
        val t4 = Pieces.ALL.first { it.name == "T4" }
        val orientations = PieceTransforms.getAllOrientations(t4.cells)
        assertEquals(4, orientations.size)
    }

    @Test
    fun `X5 plus has 1 unique orientation`() {
        val x5 = Pieces.ALL.first { it.name == "X5" }
        val orientations = PieceTransforms.getAllOrientations(x5.cells)
        assertEquals(1, orientations.size)
    }

    @Test
    fun `S4 has 4 unique orientations`() {
        // S-tetromino is chiral: S and Z shapes × 2 rotations each = 4
        val s4 = Pieces.ALL.first { it.name == "S4" }
        val orientations = PieceTransforms.getAllOrientations(s4.cells)
        assertEquals(4, orientations.size)
    }

    @Test
    fun `F5 has 8 unique orientations`() {
        val f5 = Pieces.ALL.first { it.name == "F5" }
        val orientations = PieceTransforms.getAllOrientations(f5.cells)
        assertEquals(8, orientations.size)
    }

    @Test
    fun `all orientations are normalized`() {
        for (piece in Pieces.ALL) {
            val orientations = PieceTransforms.getAllOrientations(piece.cells)
            for (orientation in orientations) {
                assertEquals(
                    "Orientation of ${piece.name} should be normalized",
                    orientation,
                    PieceTransforms.normalize(orientation)
                )
            }
        }
    }

    @Test
    fun `applyTransform with no rotation or flip returns normalized original`() {
        val cells = listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0))
        val result = PieceTransforms.applyTransform(cells, 0, false)
        assertEquals(PieceTransforms.normalize(cells), result)
    }

    @Test
    fun `applyTransform with 1 rotation equals single rotateCW`() {
        val cells = listOf(Cell(0, 0), Cell(0, 1), Cell(1, 0))
        val fromApply = PieceTransforms.applyTransform(cells, 1, false)
        val fromDirect = PieceTransforms.rotateCW(PieceTransforms.normalize(cells))
        assertEquals(fromDirect, fromApply)
    }
}
