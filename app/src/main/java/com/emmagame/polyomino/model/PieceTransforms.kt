package com.emmagame.polyomino.model

object PieceTransforms {

    private val orientationCache = HashMap<String, List<List<Cell>>>()

    fun rotateCW(cells: List<Cell>): List<Cell> =
        normalize(cells.map { Cell(it.col, -it.row) })

    fun rotateCCW(cells: List<Cell>): List<Cell> =
        normalize(cells.map { Cell(-it.col, it.row) })

    fun flipH(cells: List<Cell>): List<Cell> =
        normalize(cells.map { Cell(it.row, -it.col) })

    fun normalize(cells: List<Cell>): List<Cell> {
        val minRow = cells.minOf { it.row }
        val minCol = cells.minOf { it.col }
        return cells
            .map { Cell(it.row - minRow, it.col - minCol) }
            .sortedWith(compareBy({ it.row }, { it.col }))
    }

    fun cellsKey(cells: List<Cell>): String =
        cells.joinToString(";") { "${it.row},${it.col}" }

    fun getAllOrientations(cells: List<Cell>): List<List<Cell>> {
        val normalized = normalize(cells)
        val key = cellsKey(normalized)
        orientationCache[key]?.let { return it }

        val seen = mutableSetOf<String>()
        val orientations = mutableListOf<List<Cell>>()

        var current = normalized
        repeat(4) {
            val n = normalize(current)
            val k = cellsKey(n)
            if (seen.add(k)) orientations.add(n)

            val flipped = flipH(current)
            val fn = normalize(flipped)
            val fk = cellsKey(fn)
            if (seen.add(fk)) orientations.add(fn)

            current = rotateCW(current)
        }

        orientationCache[key] = orientations
        return orientations
    }

    fun applyTransform(
        cells: List<Cell>,
        rotations: Int,
        flipped: Boolean
    ): List<Cell> {
        var result = normalize(cells)
        if (flipped) result = flipH(result)
        repeat(((rotations % 4) + 4) % 4) {
            result = rotateCW(result)
        }
        return result
    }
}
