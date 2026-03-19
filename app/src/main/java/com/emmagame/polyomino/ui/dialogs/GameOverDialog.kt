package com.emmagame.polyomino.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emmagame.polyomino.engine.Scoring
import com.emmagame.polyomino.ui.theme.*

@Composable
fun GameOverDialog(
    rankings: List<Scoring.PlayerRanking>,
    elapsedSeconds: Int,
    onPlayAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    val playerColors = LocalPlayerColors.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rankings
                for (ranking in rankings) {
                    val color = if (ranking.playerIndex < playerColors.size) {
                        playerColors[ranking.playerIndex]
                    } else playerColors[0]

                    val isWinner = ranking.rank == 1 && !ranking.isSharedColor

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isWinner) Modifier.background(
                                    WarningColor.copy(alpha = 0.1f),
                                    MaterialTheme.shapes.small
                                ) else Modifier
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank
                        if (ranking.isSharedColor) {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted,
                                modifier = Modifier.width(24.dp)
                            )
                        } else if (isWinner) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Winner",
                                tint = WarningColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "#${ranking.rank}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted,
                                modifier = Modifier.width(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Color dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .drawBehind {
                                    drawRect(
                                        color = color.base,
                                        size = Size(size.width, size.height)
                                    )
                                }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Name
                        Text(
                            text = ranking.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        // Score
                        Text(
                            text = "${ranking.score}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                Text(
                    text = "Duration: ${minutes}m ${seconds}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Play again button
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = TextOnPlayer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play Again", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
