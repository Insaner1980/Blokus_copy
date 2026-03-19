package com.emmagame.polyomino.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emmagame.polyomino.ui.theme.*

@Composable
fun SettingsSheet(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onShowHelp: () -> Unit,
    onShowProfiles: () -> Unit,
    onShowHistory: () -> Unit,
    onSaveGame: () -> Unit,
    onNewGame: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // Sound toggle
                SettingsRow(
                    icon = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    label = "Sound",
                    trailing = {
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { onSoundToggle(it) }
                        )
                    }
                )

                // Haptics toggle
                SettingsRow(
                    icon = Icons.Default.Vibration,
                    label = "Haptics",
                    trailing = {
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = { onHapticsToggle(it) }
                        )
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Profiles
                SettingsRow(
                    icon = Icons.Default.Person,
                    label = "Profiles",
                    onClick = { onShowProfiles(); onDismiss() }
                )

                // History & Stats
                SettingsRow(
                    icon = Icons.Default.BarChart,
                    label = "History & Stats",
                    onClick = { onShowHistory(); onDismiss() }
                )

                // Help
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Help,
                    label = "Help",
                    onClick = { onShowHelp(); onDismiss() }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Save game
                SettingsRow(
                    icon = Icons.Default.Save,
                    label = "Save Game",
                    onClick = { onSaveGame(); onDismiss() }
                )

                // New game
                SettingsRow(
                    icon = Icons.Default.Add,
                    label = "New Game",
                    labelColor = DangerColor,
                    onClick = { onNewGame(); onDismiss() }
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    labelColor: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}
