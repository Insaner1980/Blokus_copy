package com.emmagame.polyomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.model.Player
import com.emmagame.polyomino.ui.theme.LocalPlayerColors
import com.emmagame.polyomino.ui.theme.TextMuted

@Composable
fun PlayerInfoBar(
    players: List<Player>,
    currentPlayerIndex: Int,
    modifier: Modifier = Modifier
) {
    val playerColors = LocalPlayerColors.current

    when (players.size) {
        2 -> {
            // Single row for 2 players
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (player in players) {
                    val colorIdx = player.index.coerceIn(0, playerColors.lastIndex)
                    PlayerInfoItem(
                        player = player,
                        color = playerColors[colorIdx].base,
                        isActive = player.index == currentPlayerIndex,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        else -> {
            // 2×2 grid for 3-4 players
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    for (i in 0..minOf(1, players.lastIndex)) {
                        val colorIdx = players[i].index.coerceIn(0, playerColors.lastIndex)
                        PlayerInfoItem(
                            player = players[i],
                            color = playerColors[colorIdx].base,
                            isActive = players[i].index == currentPlayerIndex,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    for (i in 2..minOf(3, players.lastIndex)) {
                        val colorIdx = players[i].index.coerceIn(0, playerColors.lastIndex)
                        PlayerInfoItem(
                            player = players[i],
                            color = playerColors[colorIdx].base,
                            isActive = players[i].index == currentPlayerIndex,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerInfoItem(
    player: Player,
    color: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (player.passed) 0.4f else 1f

    Row(
        modifier = modifier
            .alpha(alpha)
            .then(
                if (isActive) Modifier.background(
                    color.copy(alpha = 0.12f),
                    MaterialTheme.shapes.small
                ) else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .drawBehind {
                    drawRect(color = color, size = Size(size.width, size.height))
                }
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Name (+ shared indicator)
        val displayName = if (player.isSharedColor) "Shared" else player.name
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (player.passed) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )

        // AI/Human indicator for multiplayer
        if (player.isAi) {
            Text(
                text = "AI",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            text = "${player.score}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
