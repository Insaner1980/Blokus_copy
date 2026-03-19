package com.emmagame.polyomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.model.PieceDef
import com.emmagame.polyomino.model.PieceTransforms
import com.emmagame.polyomino.model.Pieces
import com.emmagame.polyomino.ui.theme.CardSurface
import com.emmagame.polyomino.ui.theme.LocalPlayerColors
import com.emmagame.polyomino.ui.theme.PlayerColor
import com.emmagame.polyomino.ui.theme.TextSecondary

@Composable
fun PiecePanel(
    usedPieces: BooleanArray,
    selectedPieceIndex: Int?,
    currentPlayerIndex: Int,
    currentShape: List<com.emmagame.polyomino.model.Cell>?,
    previewRotation: Int,
    previewFlip: Boolean,
    onPieceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerColors = LocalPlayerColors.current
    val playerColor = playerColors[currentPlayerIndex]

    Column(modifier = modifier) {
        // Selected piece preview
        if (selectedPieceIndex != null && currentShape != null) {
            PiecePreview(
                pieceName = Pieces.ALL[selectedPieceIndex].name,
                shape = currentShape,
                playerColor = playerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Piece grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(Pieces.ALL) { index, piece ->
                PieceCard(
                    piece = piece,
                    playerColor = playerColor,
                    isSelected = index == selectedPieceIndex,
                    isUsed = usedPieces[index],
                    onClick = { onPieceSelected(index) }
                )
            }
        }
    }
}

@Composable
fun PiecePreview(
    pieceName: String,
    shape: List<com.emmagame.polyomino.model.Cell>,
    playerColor: PlayerColor,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(CardSurface, MaterialTheme.shapes.small)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pieceName,
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(12.dp))
        PieceMiniGrid(
            cells = shape,
            color = playerColor.base,
            gridSize = 44.dp
        )
    }
}
