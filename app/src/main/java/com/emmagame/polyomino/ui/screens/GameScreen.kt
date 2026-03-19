package com.emmagame.polyomino.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.emmagame.polyomino.ui.components.ControlBar
import com.emmagame.polyomino.ui.components.GameBoard
import com.emmagame.polyomino.ui.components.PiecePanel
import com.emmagame.polyomino.ui.components.PlayerInfoBar
import com.emmagame.polyomino.ui.components.TurnStatus
import com.emmagame.polyomino.ui.dialogs.GameOverDialog
import com.emmagame.polyomino.ui.dialogs.HelpDialog
import com.emmagame.polyomino.ui.dialogs.HistoryStatsDialog
import com.emmagame.polyomino.ui.dialogs.ProfileDialog
import com.emmagame.polyomino.ui.dialogs.ResumeGameDialog
import com.emmagame.polyomino.ui.sheets.SettingsSheet
import com.emmagame.polyomino.ui.theme.AppBackground
import com.emmagame.polyomino.ui.theme.TextMuted
import com.emmagame.polyomino.ui.util.HapticManager
import com.emmagame.polyomino.ui.util.SoundManager
import com.emmagame.polyomino.ui.util.shakeAnimation
import com.emmagame.polyomino.viewmodel.GameEvent
import com.emmagame.polyomino.viewmodel.GameViewModel
import com.emmagame.polyomino.viewmodel.MultiplayerViewModel
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    multiplayerViewModel: MultiplayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameState = uiState.gameState
    val lobbyState by multiplayerViewModel.lobbyState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Sound & haptic managers
    val soundManager = remember { SoundManager(context) }
    val hapticManager = remember { HapticManager(context) }
    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    // Shake state for invalid placement
    var boardShaking by remember { mutableStateOf(false) }

    // Collect events for sound/haptic feedback
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameEvent.PiecePlaced -> {
                    soundManager.playPlacement()
                    hapticManager.confirmPlacement(view)
                }
                is GameEvent.InvalidPlacement -> {
                    soundManager.playInvalid()
                    hapticManager.rejectPlacement(view)
                    boardShaking = true
                    delay(400)
                    boardShaking = false
                }
                is GameEvent.GameSaved -> {
                    hapticManager.tick(view)
                }
                else -> { }
            }
        }
    }

    // Resume dialog
    if (uiState.showResumeDialog) {
        ResumeGameDialog(
            onResume = { viewModel.resumeSavedGame() },
            onNewGame = { viewModel.dismissResumeDialog() }
        )
    }

    // Lobby
    if (lobbyState.isActive && !lobbyState.gameStarted) {
        LobbyScreen(
            gameMode = lobbyState.gameMode,
            difficulty = lobbyState.difficulty,
            isHost = lobbyState.isHost,
            connectionState = lobbyState.connectionState,
            players = lobbyState.players,
            onStart = { multiplayerViewModel.startMultiplayerGame() },
            onBack = { multiplayerViewModel.leaveLobby() }
        )
        return
    }

    if (lobbyState.gameStarted && lobbyState.initialGameState != null) {
        val initialState = lobbyState.initialGameState!!
        viewModel.loadMultiplayerGame(initialState)
        multiplayerViewModel.leaveLobby()
    }

    if (uiState.showModeSelection) {
        ModeSelectionScreen(
            onStartGame = { mode, difficulty, slots ->
                viewModel.startNewGame(mode, difficulty, slots)
            },
            onShowProfiles = { viewModel.showProfiles() },
            onShowHistory = { viewModel.showHistoryStats() },
            onHostGame = { mode, difficulty ->
                val name = uiState.activeProfile?.name ?: "Player"
                multiplayerViewModel.hostGame(mode, difficulty, name)
            },
            onJoinGame = { mode, difficulty ->
                val name = uiState.activeProfile?.name ?: "Player"
                multiplayerViewModel.joinGame(mode, difficulty, name)
            },
            activeProfile = uiState.activeProfile,
            modifier = modifier
        )
    } else {
        val currentPlayer = gameState.players[gameState.currentPlayerIndex]

        if (isLandscape) {
            LandscapeGameLayout(viewModel, uiState, currentPlayer, boardShaking)
        } else {
            PortraitGameLayout(viewModel, uiState, currentPlayer, boardShaking)
        }
    }

    // Dialogs
    if (uiState.showGameOver) {
        GameOverDialog(
            rankings = viewModel.getRankings(),
            elapsedSeconds = uiState.elapsedSeconds,
            onPlayAgain = { viewModel.showModeSelection() },
            onDismiss = { viewModel.dismissGameOver() }
        )
    }

    if (uiState.showHistoryStats) {
        HistoryStatsDialog(
            history = uiState.history,
            stats = uiState.stats,
            onDismiss = { viewModel.dismissHistoryStats() }
        )
    }

    if (uiState.showProfiles) {
        ProfileDialog(
            profiles = uiState.profiles,
            onSetActive = { viewModel.setActiveProfile(it) },
            onAddProfile = { name, color, style -> viewModel.addProfile(name, color, style) },
            onDeleteProfile = { viewModel.deleteProfile(it) },
            onDismiss = { viewModel.dismissProfiles() }
        )
    }

    if (uiState.showSettings) {
        SettingsSheet(
            soundEnabled = uiState.soundEnabled,
            hapticsEnabled = uiState.hapticsEnabled,
            onSoundToggle = { viewModel.toggleSound(); soundManager.enabled = it },
            onHapticsToggle = { viewModel.toggleHaptics(); hapticManager.enabled = it },
            onShowHelp = { viewModel.showHelp() },
            onShowProfiles = { viewModel.showProfiles() },
            onShowHistory = { viewModel.showHistoryStats() },
            onSaveGame = { viewModel.saveGame() },
            onNewGame = { viewModel.showModeSelection() },
            onDismiss = { viewModel.dismissSettings() }
        )
    }

    if (uiState.showHelp) {
        HelpDialog(onDismiss = { viewModel.dismissHelp() })
    }
}

@Composable
private fun PortraitGameLayout(
    viewModel: GameViewModel,
    uiState: com.emmagame.polyomino.viewmodel.GameUiState,
    currentPlayer: com.emmagame.polyomino.model.Player,
    boardShaking: Boolean
) {
    val gameState = uiState.gameState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopBar(viewModel, uiState)

        PlayerInfoBar(
            players = gameState.players,
            currentPlayerIndex = gameState.currentPlayerIndex
        )

        GameBoard(
            board = gameState.board,
            startPositions = gameState.startPositions,
            currentPlayerIndex = gameState.currentPlayerIndex,
            ghostCells = uiState.ghostCells,
            ghostValid = uiState.ghostValid,
            onCellTap = { cell ->
                if (uiState.selectedPieceIndex != null) viewModel.attemptPlacement(cell)
            },
            onCellHover = { cell -> viewModel.updateGhost(cell) },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .shakeAnimation(boardShaking)
        )

        TurnStatus(
            statusText = viewModel.getCurrentTurnLabel(),
            currentPlayerIndex = gameState.currentPlayerIndex,
            isAiThinking = uiState.isAiThinking,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        ControlBar(
            hasPieceSelected = uiState.selectedPieceIndex != null,
            isGameOver = gameState.isGameOver,
            onRotateCCW = viewModel::rotateCCW,
            onRotateCW = viewModel::rotateCW,
            onFlip = viewModel::flipH,
            onPass = viewModel::passTurn
        )

        PiecePanel(
            usedPieces = currentPlayer.usedPieces,
            selectedPieceIndex = uiState.selectedPieceIndex,
            currentPlayerIndex = gameState.currentPlayerIndex,
            currentShape = uiState.currentShape,
            previewRotation = uiState.previewRotation,
            previewFlip = uiState.previewFlip,
            onPieceSelected = viewModel::selectPiece,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LandscapeGameLayout(
    viewModel: GameViewModel,
    uiState: com.emmagame.polyomino.viewmodel.GameUiState,
    currentPlayer: com.emmagame.polyomino.model.Player,
    boardShaking: Boolean
) {
    val gameState = uiState.gameState

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Left panel: logo, timer, players, status
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            TopBar(viewModel, uiState)

            PlayerInfoBar(
                players = gameState.players,
                currentPlayerIndex = gameState.currentPlayerIndex
            )

            Spacer(modifier = Modifier.weight(1f))

            TurnStatus(
                statusText = viewModel.getCurrentTurnLabel(),
                currentPlayerIndex = gameState.currentPlayerIndex,
                isAiThinking = uiState.isAiThinking
            )
        }

        // Center: board
        GameBoard(
            board = gameState.board,
            startPositions = gameState.startPositions,
            currentPlayerIndex = gameState.currentPlayerIndex,
            ghostCells = uiState.ghostCells,
            ghostValid = uiState.ghostValid,
            onCellTap = { cell ->
                if (uiState.selectedPieceIndex != null) viewModel.attemptPlacement(cell)
            },
            onCellHover = { cell -> viewModel.updateGhost(cell) },
            modifier = Modifier
                .weight(0.45f)
                .padding(vertical = 8.dp)
                .shakeAnimation(boardShaking)
        )

        // Right panel: controls, preview, pieces
        Column(
            modifier = Modifier
                .weight(0.30f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            ControlBar(
                hasPieceSelected = uiState.selectedPieceIndex != null,
                isGameOver = gameState.isGameOver,
                onRotateCCW = viewModel::rotateCCW,
                onRotateCW = viewModel::rotateCW,
                onFlip = viewModel::flipH,
                onPass = viewModel::passTurn
            )

            PiecePanel(
                usedPieces = currentPlayer.usedPieces,
                selectedPieceIndex = uiState.selectedPieceIndex,
                currentPlayerIndex = gameState.currentPlayerIndex,
                currentShape = uiState.currentShape,
                previewRotation = uiState.previewRotation,
                previewFlip = uiState.previewFlip,
                onPieceSelected = viewModel::selectPiece,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TopBar(
    viewModel: GameViewModel,
    uiState: com.emmagame.polyomino.viewmodel.GameUiState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Polyomino",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        val minutes = uiState.elapsedSeconds / 60
        val seconds = uiState.elapsedSeconds % 60
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Timer",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "%d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )

        IconButton(onClick = { viewModel.showSettings() }) {
            Icon(Icons.Default.Menu, contentDescription = "Settings", tint = TextMuted)
        }
    }
}
