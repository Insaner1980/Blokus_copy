package com.emmagame.polyomino.ai

import com.emmagame.polyomino.model.Board
import com.emmagame.polyomino.model.Cell
import com.emmagame.polyomino.model.GameState

object Territory {

    private val ORTHO_OFFSETS = arrayOf(
        intArrayOf(-1, 0), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(0, 1)
    )

    fun estimateTerritory(state: GameState, playerIndex: Int): Int {
        val board = state.board
        val visited = BooleanArray(board.size * board.size)
        var count = 0

        // Find all corner positions (reachable starting points)
        val starts = mutableListOf<Int>()
        for (idx in board.cells.indices) {
            val row = board.row(idx)
            val col = board.col(idx)
            if (board.cells[idx] != Board.EMPTY) continue

            // Check diagonal adjacency to player
            var diagAdj = false
            var orthoAdj = false
            for (dr in intArrayOf(-1, 1)) {
                for (dc in intArrayOf(-1, 1)) {
                    val nr = row + dr
                    val nc = col + dc
                    if (board.inBounds(nr, nc) && board.get(nr, nc) == playerIndex) {
                        diagAdj = true
                    }
                }
            }
            for (offset in ORTHO_OFFSETS) {
                val nr = row + offset[0]
                val nc = col + offset[1]
                if (board.inBounds(nr, nc) && board.get(nr, nc) == playerIndex) {
                    orthoAdj = true
                }
            }
            if (diagAdj && !orthoAdj) {
                starts.add(idx)
            }
        }

        // BFS flood fill from corner positions
        val queue = ArrayDeque<Int>(starts.size * 4)
        for (start in starts) {
            if (!visited[start]) {
                visited[start] = true
                queue.addLast(start)
                count++
            }
        }

        while (queue.isNotEmpty()) {
            val idx = queue.removeFirst()
            val row = board.row(idx)
            val col = board.col(idx)

            for (offset in ORTHO_OFFSETS) {
                val nr = row + offset[0]
                val nc = col + offset[1]
                if (!board.inBounds(nr, nc)) continue
                val nIdx = board.index(nr, nc)
                if (visited[nIdx]) continue
                if (board.cells[nIdx] != Board.EMPTY) continue

                // Don't cross into cells orthogonally adjacent to our own pieces
                // (we can't place there anyway)
                var blockedByOrtho = false
                for (o2 in ORTHO_OFFSETS) {
                    val nnr = nr + o2[0]
                    val nnc = nc + o2[1]
                    if (board.inBounds(nnr, nnc) && board.get(nnr, nnc) == playerIndex) {
                        blockedByOrtho = true
                        break
                    }
                }
                if (blockedByOrtho) continue

                visited[nIdx] = true
                queue.addLast(nIdx)
                count++
            }
        }

        return count
    }
}
