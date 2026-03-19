package com.emmagame.polyomino.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emmagame.polyomino.data.GameStats
import com.emmagame.polyomino.data.HistoryEntry
import com.emmagame.polyomino.ui.theme.*

@Composable
fun HistoryStatsDialog(
    history: List<HistoryEntry>,
    stats: GameStats,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("History") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Stats") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> HistoryTab(history)
                    1 -> StatsTab(stats)
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(history: List<HistoryEntry>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No games played yet", color = TextMuted)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(history) { entry ->
            HistoryRow(entry)
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    val isWin = entry.rank == 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isWin) WarningColor.copy(alpha = 0.06f) else AppBackground,
                MaterialTheme.shapes.small
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        if (isWin) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = WarningColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                "#${entry.rank}",
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted,
                modifier = Modifier.width(20.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.gameMode.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row {
                Text(
                    entry.date.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${entry.time / 60}m ${entry.time % 60}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Difficulty stars
        Text(
            "★".repeat(entry.difficulty),
            style = MaterialTheme.typography.labelSmall,
            color = WarningColor
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "${entry.score}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatsTab(stats: GameStats) {
    if (stats.totalGames == 0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No stats yet", color = TextMuted)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            // Overview stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox("Games", "${stats.totalGames}")
                StatBox("Wins", "${stats.wins}")
                StatBox("Win %", "%.0f%%".format(stats.winRate * 100))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox("Avg Score", "%.1f".format(stats.averageScore))
                StatBox("Best", "${stats.bestScore}")
                StatBox("Avg Rank", "%.1f".format(stats.averageRank))
            }
        }

        // Score trend chart
        if (stats.recentScores.isNotEmpty()) {
            item {
                Text(
                    "Recent Scores",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                ScoreTrendChart(
                    scores = stats.recentScores,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }

        // Per-difficulty stats
        if (stats.statsByDifficulty.isNotEmpty()) {
            item {
                Text(
                    "By Difficulty",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }
            for ((level, diffStats) in stats.statsByDifficulty.toSortedMap()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppBackground, MaterialTheme.shapes.small)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "★".repeat(level),
                            color = WarningColor,
                            modifier = Modifier.width(60.dp)
                        )
                        Text(
                            "${diffStats.games} games",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${diffStats.wins}W",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessColor
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "avg %.1f".format(diffStats.avgScore),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(AppBackground, MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AccentColor
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun ScoreTrendChart(
    scores: List<Int>,
    modifier: Modifier = Modifier
) {
    val playerColors = LocalPlayerColors.current
    val barColor = playerColors[0].base

    Canvas(modifier = modifier) {
        if (scores.isEmpty()) return@Canvas
        val maxScore = scores.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
        val barWidth = size.width / scores.size
        val gap = barWidth * 0.15f

        for ((i, score) in scores.withIndex()) {
            val barHeight = (score / maxScore) * size.height * 0.9f
            drawRect(
                color = barColor,
                topLeft = Offset(
                    i * barWidth + gap / 2,
                    size.height - barHeight
                ),
                size = Size(barWidth - gap, barHeight)
            )
        }
    }
}
