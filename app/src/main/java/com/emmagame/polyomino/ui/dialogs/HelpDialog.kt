package com.emmagame.polyomino.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emmagame.polyomino.ui.theme.CardSurface
import com.emmagame.polyomino.ui.theme.TextMuted
import com.emmagame.polyomino.ui.theme.TextSecondary

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "How to Play",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                HelpSection("Goal") {
                    "Place as many of your 21 polyomino pieces on the board as possible. " +
                        "The player with the fewest remaining cells (lowest score) wins."
                }

                HelpSection("Placement Rules") {
                    "1. Your first piece must cover your assigned corner position.\n" +
                        "2. Each subsequent piece must touch at least one of your existing pieces diagonally (corner-to-corner).\n" +
                        "3. Pieces may NOT touch your own pieces along an edge (orthogonally).\n" +
                        "4. Pieces can touch other players' pieces in any way.\n" +
                        "5. All cells of the piece must be within the board."
                }

                HelpSection("Controls") {
                    "Tap a piece from the panel below to select it.\n" +
                        "Use the rotate and flip buttons to orient it.\n" +
                        "Tap the board to place the piece.\n" +
                        "The ghost preview shows where the piece will land — green means valid, red means invalid.\n" +
                        "Tap Pass to skip your turn (you cannot play again after passing)."
                }

                HelpSection("Game Modes") {
                    "Solo: 1 human vs 3 AI on a 20\u00d720 board.\n" +
                        "Two-Player Standard: 2 humans, each controls 2 colors (20\u00d720).\n" +
                        "Duo: 2 players on a compact 14\u00d714 board.\n" +
                        "Three-Player: 3 players + 1 shared color that rotates.\n" +
                        "Four-Player: 4 human players, classic rules."
                }

                HelpSection("AI Difficulty") {
                    "\u2605 Beginner: Makes obvious mistakes, good for learning.\n" +
                        "\u2605\u2605 Easy: Plays logically but misses opportunities.\n" +
                        "\u2605\u2605\u2605 Medium: Competent play, blocks when obvious.\n" +
                        "\u2605\u2605\u2605\u2605 Hard: Proactive blocking, territory management.\n" +
                        "\u2605\u2605\u2605\u2605\u2605 Expert: Near-optimal play, very difficult to beat."
                }

                HelpSection("Scoring") {
                    "Each player starts with 89 points (total cells across all 21 pieces). " +
                        "Every cell you place reduces your score by 1. Lower score = better. " +
                        "A perfect game (all pieces placed) scores 0."
                }

                HelpSection("Multiplayer") {
                    "Host Online: Advertise your game for nearby players to find.\n" +
                        "Join Online: Search for and connect to a hosted game.\n" +
                        "Uses Bluetooth and WiFi for peer-to-peer connection — no internet required."
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, content: () -> String) {
    Spacer(Modifier.height(12.dp))
    Text(
        title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary
    )
    Spacer(Modifier.height(4.dp))
    Text(
        content(),
        style = MaterialTheme.typography.bodyMedium,
        color = TextMuted
    )
}
