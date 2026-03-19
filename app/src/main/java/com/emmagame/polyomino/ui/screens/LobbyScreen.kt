package com.emmagame.polyomino.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.model.Difficulty
import com.emmagame.polyomino.model.GameMode
import com.emmagame.polyomino.multiplayer.ConnectionState
import com.emmagame.polyomino.multiplayer.SessionPlayer
import com.emmagame.polyomino.ui.theme.*

@Composable
fun LobbyScreen(
    gameMode: GameMode,
    difficulty: Difficulty,
    isHost: Boolean,
    connectionState: ConnectionState,
    players: List<SessionPlayer>,
    onStart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerColors = LocalPlayerColors.current
    val requiredPlayers = when (gameMode) {
        GameMode.TWO_PLAYER_DUO -> 2
        GameMode.THREE_PLAYER -> 3
        GameMode.FOUR_PLAYER -> 4
        GameMode.TWO_PLAYER_STANDARD -> 2
        else -> 2
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (isHost) "Hosting Game" else "Joining Game",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        // Game info
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(gameMode.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("${gameMode.boardSize}×${gameMode.boardSize} board", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Text("Difficulty: ${"★".repeat(difficulty.level)}", style = MaterialTheme.typography.labelMedium, color = WarningColor)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Connection status
        ConnectionStatusIndicator(connectionState)

        Spacer(Modifier.height(16.dp))

        // Player slots
        Text("Players", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val totalSlots = gameMode.playerCount
        for (i in 0 until totalSlots) {
            val player = players.getOrNull(i)
            val colorIdx = i.coerceIn(0, playerColors.lastIndex)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .drawBehind {
                            drawRect(
                                color = playerColors[colorIdx].base,
                                size = Size(size.width, size.height)
                            )
                        }
                )

                Spacer(Modifier.width(12.dp))

                if (player != null) {
                    Icon(
                        if (player.isAi) Icons.Default.SmartToy else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (player.isLocal) AccentColor else SuccessColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        player.name + if (player.isLocal) " (You)" else "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        "Waiting for player...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.weight(1f))

        // Start button (host only)
        if (isHost) {
            val canStart = players.size >= requiredPlayers ||
                (players.size >= 2 && connectionState == ConnectionState.CONNECTED)

            Button(
                onClick = onStart,
                enabled = canStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = TextOnPlayer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Start Game",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                "Waiting for host to start...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(state: ConnectionState) {
    val (text, color) = when (state) {
        ConnectionState.DISCONNECTED -> "Disconnected" to DangerColor
        ConnectionState.DISCOVERING -> "Searching for players..." to WarningColor
        ConnectionState.CONNECTING -> "Connecting..." to WarningColor
        ConnectionState.CONNECTED -> "Connected" to SuccessColor
        ConnectionState.RECONNECTING -> "Reconnecting..." to WarningColor
        ConnectionState.FAILED -> "Connection failed" to DangerColor
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        if (state == ConnectionState.DISCOVERING || state == ConnectionState.CONNECTING || state == ConnectionState.RECONNECTING) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = color
            )
            Spacer(Modifier.width(8.dp))
        }

        Icon(
            Icons.Default.Wifi,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
