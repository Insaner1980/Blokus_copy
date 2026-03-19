package com.emmagame.polyomino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.emmagame.polyomino.ui.screens.GameScreen
import com.emmagame.polyomino.ui.theme.PolyominoTheme
import com.emmagame.polyomino.viewmodel.GameViewModel
import com.emmagame.polyomino.viewmodel.MultiplayerViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private val multiplayerViewModel: MultiplayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolyominoTheme {
                GameScreen(
                    viewModel = gameViewModel,
                    multiplayerViewModel = multiplayerViewModel
                )
            }
        }
    }
}
