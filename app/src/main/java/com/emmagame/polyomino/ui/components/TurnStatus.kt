package com.emmagame.polyomino.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.ui.theme.LocalPlayerColors
import com.emmagame.polyomino.ui.util.rememberThinkingDots

@Composable
fun TurnStatus(
    statusText: String,
    currentPlayerIndex: Int,
    isAiThinking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val playerColors = LocalPlayerColors.current
    val currentColor = if (currentPlayerIndex < playerColors.size) {
        playerColors[currentPlayerIndex].base
    } else {
        playerColors[0].base
    }

    val thinkingDots = if (isAiThinking) rememberThinkingDots() else ""
    val displayText = if (isAiThinking) statusText + thinkingDots else statusText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = currentColor,
                    topLeft = Offset.Zero,
                    size = Size(4.dp.toPx(), size.height)
                )
            }
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
