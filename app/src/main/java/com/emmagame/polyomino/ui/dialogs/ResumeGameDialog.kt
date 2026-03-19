package com.emmagame.polyomino.ui.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ResumeGameDialog(
    onResume: () -> Unit,
    onNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onNewGame,
        title = {
            Text(
                "Saved Game Found",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("You have a saved game in progress. Would you like to continue?")
        },
        confirmButton = {
            Button(onClick = onResume) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onNewGame) {
                Text("New Game")
            }
        }
    )
}
