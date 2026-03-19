package com.emmagame.polyomino.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.emmagame.polyomino.model.Board
import com.emmagame.polyomino.model.Cell
import com.emmagame.polyomino.ui.theme.LocalPlayerColors

@Composable
fun GameBoard(
    board: Board,
    startPositions: List<Cell>,
    currentPlayerIndex: Int,
    ghostCells: List<Cell> = emptyList(),
    ghostValid: Boolean = true,
    onCellTap: (Cell) -> Unit = {},
    onCellHover: (Cell?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val playerColors = LocalPlayerColors.current
    var cachedLayout by remember { mutableStateOf<BoardLayout?>(null) }

    val occupiedCount = board.cells.count { it != Board.EMPTY }
    val boardDesc = "Game board, ${board.size} by ${board.size}, $occupiedCount cells occupied"

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = boardDesc }
            .pointerInput(board.size) {
                detectTapGestures { offset ->
                    val layout = cachedLayout ?: BoardLayout.calculate(
                        size.width.toFloat(), board.size
                    )
                    val cell = layout.pixelToCell(offset.x, offset.y)
                    if (cell != null) {
                        onCellTap(cell)
                    }
                }
            }
            .pointerInput(board.size) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val layout = cachedLayout ?: BoardLayout.calculate(
                            size.width.toFloat(), board.size
                        )
                        onCellHover(layout.pixelToCell(offset.x, offset.y))
                    },
                    onDrag = { change, _ ->
                        val layout = cachedLayout ?: BoardLayout.calculate(
                            size.width.toFloat(), board.size
                        )
                        val cell = layout.pixelToCell(change.position.x, change.position.y)
                        onCellHover(cell)
                        change.consume()
                    },
                    onDragEnd = { onCellHover(null) },
                    onDragCancel = { onCellHover(null) }
                )
            }
    ) {
        val layout = BoardLayout.calculate(size.width, board.size)
        cachedLayout = layout

        drawBoardBackground(layout)
        drawBoardFrame(layout)
        drawCells(layout, board, playerColors)
        drawStartPositions(layout, board, startPositions, playerColors, currentPlayerIndex)

        if (ghostCells.isNotEmpty() && currentPlayerIndex < playerColors.size) {
            drawGhostCells(
                layout, ghostCells, ghostValid,
                playerColors[currentPlayerIndex]
            )
        }
    }
}
