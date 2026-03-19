package com.emmagame.polyomino.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emmagame.polyomino.data.Profile
import com.emmagame.polyomino.model.GameConstants
import com.emmagame.polyomino.ui.theme.*

@Composable
fun ProfileDialog(
    profiles: List<Profile>,
    onSetActive: (Long) -> Unit,
    onAddProfile: (String, Int, String) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Profiles",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add profile")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (profiles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No profiles yet", color = TextMuted)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Text("Create Profile")
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(profiles) { profile ->
                            ProfileRow(
                                profile = profile,
                                onSelect = { onSetActive(profile.id) },
                                onDelete = { onDeleteProfile(profile.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProfileDialog(
            onAdd = { name, colorIdx, avatarStyle ->
                onAddProfile(name, colorIdx, avatarStyle)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun ProfileRow(
    profile: Profile,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val playerColors = LocalPlayerColors.current
    val colorIdx = profile.colorIndex.coerceIn(0, playerColors.lastIndex)
    val color = playerColors[colorIdx]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (profile.active) color.base.copy(alpha = 0.08f) else AppBackground,
                MaterialTheme.shapes.small
            )
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .drawBehind {
                    drawRect(color = color.base, size = Size(size.width, size.height))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                profile.name.take(1).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = TextOnPlayer
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (profile.active) FontWeight.Bold else FontWeight.Medium
                )
                if (profile.active) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Active",
                        tint = SuccessColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                "${profile.history.size} games played",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddProfileDialog(
    onAdd: (String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var avatarStyle by remember { mutableStateOf("bottts") }

    val playerColors = LocalPlayerColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 30) name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text("Color", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0..3) {
                        val isSelected = i == colorIndex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .drawBehind {
                                    drawRect(
                                        color = playerColors[i].base,
                                        size = Size(size.width, size.height)
                                    )
                                }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.White)
                                    else Modifier
                                )
                                .clickable { colorIndex = i },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TextOnPlayer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text("Avatar Style", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (style in GameConstants.AVATAR_STYLES.take(4)) {
                        val isSelected = style == avatarStyle
                        FilterChip(
                            selected = isSelected,
                            onClick = { avatarStyle = style },
                            label = {
                                Text(
                                    style.take(4),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name.ifBlank { "Player" }, colorIndex, avatarStyle) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
