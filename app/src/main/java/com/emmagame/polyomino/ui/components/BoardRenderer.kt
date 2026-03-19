package com.emmagame.polyomino.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.emmagame.polyomino.model.Board
import com.emmagame.polyomino.model.Cell
import com.emmagame.polyomino.ui.theme.BoardCellGap
import com.emmagame.polyomino.ui.theme.BoardCellSurface
import com.emmagame.polyomino.ui.theme.BoardFrame
import com.emmagame.polyomino.ui.theme.GhostInvalidBorder
import com.emmagame.polyomino.ui.theme.GhostInvalidFill
import com.emmagame.polyomino.ui.theme.PlayerColor

data class BoardLayout(
    val boardSize: Int,
    val frameWidth: Float,
    val gapWidth: Float,
    val cellSize: Float,
    val totalSize: Float
) {
    fun cellTopLeft(row: Int, col: Int): Offset {
        val x = frameWidth + col * (cellSize + gapWidth)
        val y = frameWidth + row * (cellSize + gapWidth)
        return Offset(x, y)
    }

    fun pixelToCell(x: Float, y: Float): Cell? {
        val bx = x - frameWidth
        val by = y - frameWidth
        if (bx < 0 || by < 0) return null

        val step = cellSize + gapWidth
        val col = (bx / step).toInt()
        val row = (by / step).toInt()

        if (row !in 0 until boardSize || col !in 0 until boardSize) return null

        // Check we're inside the cell, not in the gap
        val cellX = bx - col * step
        val cellY = by - row * step
        if (cellX > cellSize || cellY > cellSize) return null

        return Cell(row, col)
    }

    companion object {
        fun calculate(canvasSize: Float, boardSize: Int): BoardLayout {
            val frameWidth = 4f * 2.625f // 4dp at ~2.625 density
            val gapWidth = 2f * 2.625f
            val availableForCells = canvasSize - 2 * frameWidth + gapWidth
            val cellSize = (availableForCells / boardSize) - gapWidth
            return BoardLayout(
                boardSize = boardSize,
                frameWidth = frameWidth,
                gapWidth = gapWidth,
                cellSize = cellSize,
                totalSize = canvasSize
            )
        }
    }
}

// Layer 1: Background (cell gap color)
fun DrawScope.drawBoardBackground(layout: BoardLayout) {
    drawRect(
        color = BoardCellGap,
        topLeft = Offset(layout.frameWidth, layout.frameWidth),
        size = Size(
            layout.totalSize - 2 * layout.frameWidth,
            layout.totalSize - 2 * layout.frameWidth
        )
    )
}

// Layer 2: Dark frame
fun DrawScope.drawBoardFrame(layout: BoardLayout) {
    val fw = layout.frameWidth
    val ts = layout.totalSize
    // Top
    drawRect(color = BoardFrame, topLeft = Offset.Zero, size = Size(ts, fw))
    // Bottom
    drawRect(color = BoardFrame, topLeft = Offset(0f, ts - fw), size = Size(ts, fw))
    // Left
    drawRect(color = BoardFrame, topLeft = Offset(0f, fw), size = Size(fw, ts - 2 * fw))
    // Right
    drawRect(color = BoardFrame, topLeft = Offset(ts - fw, fw), size = Size(fw, ts - 2 * fw))
}

// Layer 3: Individual cells
fun DrawScope.drawCells(
    layout: BoardLayout,
    board: Board,
    playerColors: List<PlayerColor>
) {
    for (row in 0 until layout.boardSize) {
        for (col in 0 until layout.boardSize) {
            val topLeft = layout.cellTopLeft(row, col)
            val cellValue = board.get(row, col)
            val cellSize = Size(layout.cellSize, layout.cellSize)

            if (cellValue == Board.EMPTY) {
                drawRect(
                    color = BoardCellSurface,
                    topLeft = topLeft,
                    size = cellSize
                )
            } else {
                drawOccupiedCell(topLeft, layout.cellSize, playerColors[cellValue])
            }
        }
    }
}

// Layer 4: Glossy piece effect
fun DrawScope.drawOccupiedCell(
    topLeft: Offset,
    cellSize: Float,
    color: PlayerColor
) {
    val size = Size(cellSize, cellSize)

    // 1. Base fill
    drawRect(color = color.base, topLeft = topLeft, size = size)

    // 2. Top highlight — white at 35% alpha, top 25%
    drawRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = topLeft,
        size = Size(cellSize, cellSize * 0.25f)
    )

    // 3. Bottom shadow — dark color at 50% alpha, bottom 15%
    drawRect(
        color = color.dark.copy(alpha = 0.50f),
        topLeft = Offset(topLeft.x, topLeft.y + cellSize * 0.85f),
        size = Size(cellSize, cellSize * 0.15f)
    )

    // 4. Inner inset — black at 8% alpha, inset 2dp
    val inset = cellSize * 0.08f
    drawRect(
        color = Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
        size = Size(cellSize - 2 * inset, cellSize - 2 * inset)
    )

    // 5. Drop shadow (draw subtle shadow below/right the cell)
    drawRect(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(topLeft.x + 1f, topLeft.y + 1f),
        size = Size(cellSize, cellSize)
    )
    // Re-draw base on top so shadow only shows at edges
    drawRect(color = color.base, topLeft = topLeft, size = size)
    // Re-apply highlights
    drawRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = topLeft,
        size = Size(cellSize, cellSize * 0.25f)
    )
    drawRect(
        color = color.dark.copy(alpha = 0.50f),
        topLeft = Offset(topLeft.x, topLeft.y + cellSize * 0.85f),
        size = Size(cellSize, cellSize * 0.15f)
    )
    drawRect(
        color = Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
        size = Size(cellSize - 2 * inset, cellSize - 2 * inset)
    )
}

// Layer 5: Start position indicators
fun DrawScope.drawStartPositions(
    layout: BoardLayout,
    board: Board,
    startPositions: List<Cell>,
    playerColors: List<PlayerColor>,
    currentPlayerIndex: Int
) {
    for ((idx, pos) in startPositions.withIndex()) {
        if (!board.inBounds(pos.row, pos.col)) continue
        if (board.get(pos.row, pos.col) != Board.EMPTY) continue

        val topLeft = layout.cellTopLeft(pos.row, pos.col)
        val markerSize = layout.cellSize * 0.25f
        val offset = (layout.cellSize - markerSize) / 2

        val alpha = if (idx == currentPlayerIndex) 0.50f else 0.25f
        drawRect(
            color = playerColors[idx].base.copy(alpha = alpha),
            topLeft = Offset(topLeft.x + offset, topLeft.y + offset),
            size = Size(markerSize, markerSize)
        )
    }
}

// Layer 6: Ghost preview
fun DrawScope.drawGhostCells(
    layout: BoardLayout,
    ghostCells: List<Cell>,
    isValid: Boolean,
    playerColor: PlayerColor
) {
    for (cell in ghostCells) {
        val topLeft = layout.cellTopLeft(cell.row, cell.col)
        val size = Size(layout.cellSize, layout.cellSize)

        if (isValid) {
            drawRect(
                color = playerColor.ghost,
                topLeft = topLeft,
                size = size
            )
        } else {
            drawRect(
                color = GhostInvalidFill,
                topLeft = topLeft,
                size = size
            )
            // Red border for invalid
            val strokeWidth = 2f
            drawRect(
                color = GhostInvalidBorder,
                topLeft = topLeft,
                size = size,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )
        }
    }
}
