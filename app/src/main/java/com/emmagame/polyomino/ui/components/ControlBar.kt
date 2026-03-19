package com.emmagame.polyomino.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.ui.theme.DangerColor
import com.emmagame.polyomino.ui.theme.TextOnPlayer

@Composable
fun ControlBar(
    hasPieceSelected: Boolean,
    isGameOver: Boolean,
    onRotateCCW: () -> Unit,
    onRotateCW: () -> Unit,
    onFlip: () -> Unit,
    onPass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controlsEnabled = hasPieceSelected && !isGameOver

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rotate group
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            FilledTonalButton(
                onClick = onRotateCCW,
                enabled = controlsEnabled,
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 4.dp, bottomEnd = 4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                    contentDescription = "Rotate left"
                )
            }
            FilledTonalButton(
                onClick = onRotateCW,
                enabled = controlsEnabled,
                shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = "Rotate right"
                )
            }
        }

        // Flip
        FilledTonalButton(
            onClick = onFlip,
            enabled = controlsEnabled,
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flip,
                contentDescription = "Flip"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pass
        Button(
            onClick = onPass,
            enabled = !isGameOver,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DangerColor,
                contentColor = TextOnPlayer
            ),
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text("Pass", style = MaterialTheme.typography.labelLarge)
        }
    }
}
