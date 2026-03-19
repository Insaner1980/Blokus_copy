package com.emmagame.polyomino.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.model.Cell
import com.emmagame.polyomino.model.PieceDef
import com.emmagame.polyomino.model.PieceTransforms
import com.emmagame.polyomino.ui.theme.PlayerColor

@Composable
fun PieceCard(
    piece: PieceDef,
    playerColor: PlayerColor,
    isSelected: Boolean,
    isUsed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardSize: Dp = 56.dp
) {
    val alpha = if (isUsed) 0.15f else 1f
    val scale = if (isSelected) 1.08f else 1f
    val borderColor = if (isSelected) playerColor.base else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    val stateDesc = when {
        isUsed -> "used"
        isSelected -> "selected"
        else -> "available"
    }

    Box(
        modifier = modifier
            .size(cardSize.coerceAtLeast(48.dp))
            .scale(scale)
            .alpha(alpha)
            .semantics {
                contentDescription = "Piece ${piece.name}, ${piece.cells.size} cells, $stateDesc"
            }
            .then(
                if (isSelected) Modifier.border(borderWidth, borderColor) else Modifier
            )
            .clickable(enabled = !isUsed) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        PieceMiniGrid(
            cells = PieceTransforms.normalize(piece.cells),
            color = playerColor.base,
            gridSize = cardSize - 12.dp
        )
    }
}

@Composable
fun PieceMiniGrid(
    cells: List<Cell>,
    color: Color,
    gridSize: Dp,
    modifier: Modifier = Modifier
) {
    val maxRow = cells.maxOf { it.row } + 1
    val maxCol = cells.maxOf { it.col } + 1
    val maxDim = maxOf(maxRow, maxCol)

    Canvas(
        modifier = modifier.size(gridSize)
    ) {
        val cellPx = size.width / maxDim
        val gap = cellPx * 0.1f
        val effectiveCellSize = cellPx - gap

        // Center the piece in the canvas
        val offsetX = (size.width - maxCol * cellPx) / 2
        val offsetY = (size.height - maxRow * cellPx) / 2

        for (cell in cells) {
            val x = offsetX + cell.col * cellPx + gap / 2
            val y = offsetY + cell.row * cellPx + gap / 2
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(effectiveCellSize, effectiveCellSize)
            )
            // Mini highlight
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x, y),
                size = Size(effectiveCellSize, effectiveCellSize * 0.3f)
            )
        }
    }
}
