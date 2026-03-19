package com.emmagame.polyomino.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.engine.PlayerSlotConfig
import com.emmagame.polyomino.model.Difficulty
import com.emmagame.polyomino.model.GameMode
import com.emmagame.polyomino.ui.theme.*

@Composable
fun ModeSelectionScreen(
    onStartGame: (GameMode, Difficulty, List<PlayerSlotConfig>) -> Unit,
    onShowProfiles: () -> Unit = {},
    onShowHistory: () -> Unit = {},
    onHostGame: (GameMode, Difficulty) -> Unit = { _, _ -> },
    onJoinGame: (GameMode, Difficulty) -> Unit = { _, _ -> },
    activeProfile: com.emmagame.polyomino.data.Profile? = null,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(GameMode.SOLO) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var slotConfigs by remember { mutableStateOf(defaultSlotsForMode(GameMode.SOLO)) }

    LaunchedEffect(selectedMode) {
        slotConfigs = defaultSlotsForMode(selectedMode)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Polyomino",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = AccentColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Game Mode Selection
        Text(
            text = "Game Mode",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        for (mode in GameMode.entries) {
            ModeCard(
                mode = mode,
                isSelected = mode == selectedMode,
                onClick = { selectedMode = mode }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Difficulty
        Text(
            text = "AI Difficulty",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (diff in Difficulty.entries) {
                val isSelected = diff == selectedDifficulty
                FilledTonalButton(
                    onClick = { selectedDifficulty = diff },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected) AccentColor else CardSurface,
                        contentColor = if (isSelected) TextOnPlayer else TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${diff.level}",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Player Slots
        val slotCount = when (selectedMode) {
            GameMode.TWO_PLAYER_DUO -> 2
            GameMode.THREE_PLAYER -> 3
            else -> 4
        }

        if (selectedMode != GameMode.SOLO) {
            Text(
                text = "Players",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            for (i in 0 until slotCount) {
                val colorIdx = if (i < PlayerColors.size) i else 0
                PlayerSlotRow(
                    slotIndex = i,
                    playerColor = PlayerColors[colorIdx],
                    config = slotConfigs.getOrElse(i) { PlayerSlotConfig() },
                    onToggleAi = {
                        val newConfigs = slotConfigs.toMutableList()
                        while (newConfigs.size <= i) newConfigs.add(PlayerSlotConfig())
                        val current = newConfigs[i]
                        newConfigs[i] = current.copy(isAi = !current.isAi)
                        slotConfigs = newConfigs
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Start button
        Button(
            onClick = { onStartGame(selectedMode, selectedDifficulty, slotConfigs) },
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
                text = "Start Game",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Multiplayer buttons (for non-solo modes)
        if (selectedMode != GameMode.SOLO) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onHostGame(selectedMode, selectedDifficulty) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Host Online")
                }
                OutlinedButton(
                    onClick = { onJoinGame(selectedMode, selectedDifficulty) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Join Online")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile & History buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShowProfiles,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (activeProfile != null) activeProfile.name else "Profiles"
                )
            }
            OutlinedButton(
                onClick = onShowHistory,
                modifier = Modifier.weight(1f)
            ) {
                Text("History & Stats")
            }
        }
    }
}

@Composable
private fun ModeCard(
    mode: GameMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) AccentColor else Color.Transparent
    val bgColor = if (isSelected) AccentColor.copy(alpha = 0.08f) else CardSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AccentColor else TextPrimary
            )
            Text(
                text = getModeDescription(mode),
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
        }
        Text(
            text = "${mode.boardSize}×${mode.boardSize}",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
    }
}

@Composable
private fun PlayerSlotRow(
    slotIndex: Int,
    playerColor: PlayerColor,
    config: PlayerSlotConfig,
    onToggleAi: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .drawBehind {
                    drawRect(
                        color = playerColor.base,
                        size = Size(size.width, size.height)
                    )
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Player ${slotIndex + 1}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = config.isAi,
            onClick = onToggleAi,
            label = {
                Text(if (config.isAi) "AI" else "Human")
            }
        )
    }
}

private fun getModeDescription(mode: GameMode): String = when (mode) {
    GameMode.SOLO -> "1 human + 3 AI opponents"
    GameMode.TWO_PLAYER_STANDARD -> "2 players, each controls 2 colors"
    GameMode.TWO_PLAYER_DUO -> "2 players, smaller board, faster games"
    GameMode.THREE_PLAYER -> "3 players + 1 shared color"
    GameMode.FOUR_PLAYER -> "4 players, standard rules"
}

private fun defaultSlotsForMode(mode: GameMode): List<PlayerSlotConfig> = when (mode) {
    GameMode.SOLO -> listOf(
        PlayerSlotConfig(isAi = false, name = "Player"),
        PlayerSlotConfig(isAi = true, name = "Amber"),
        PlayerSlotConfig(isAi = true, name = "Coral"),
        PlayerSlotConfig(isAi = true, name = "Teal")
    )
    GameMode.TWO_PLAYER_STANDARD -> listOf(
        PlayerSlotConfig(isAi = false, name = "Indigo"),
        PlayerSlotConfig(isAi = false, name = "Amber"),
        PlayerSlotConfig(isAi = false, name = "Coral"),
        PlayerSlotConfig(isAi = false, name = "Teal")
    )
    GameMode.TWO_PLAYER_DUO -> listOf(
        PlayerSlotConfig(isAi = false, name = "Player 1"),
        PlayerSlotConfig(isAi = false, name = "Player 2")
    )
    GameMode.THREE_PLAYER -> listOf(
        PlayerSlotConfig(isAi = false, name = "Player 1"),
        PlayerSlotConfig(isAi = false, name = "Player 2"),
        PlayerSlotConfig(isAi = false, name = "Player 3")
    )
    GameMode.FOUR_PLAYER -> listOf(
        PlayerSlotConfig(isAi = false, name = "Player 1"),
        PlayerSlotConfig(isAi = false, name = "Player 2"),
        PlayerSlotConfig(isAi = false, name = "Player 3"),
        PlayerSlotConfig(isAi = false, name = "Player 4")
    )
}
