package com.emmagame.polyomino.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emmagame.polyomino.ai.AiEngine
import com.emmagame.polyomino.data.*
import com.emmagame.polyomino.engine.GameEngine
import com.emmagame.polyomino.engine.PlacementValidator
import com.emmagame.polyomino.engine.PlayerSlotConfig
import com.emmagame.polyomino.engine.Scoring
import com.emmagame.polyomino.model.*
import com.emmagame.polyomino.multiplayer.LocalSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class GameUiState(
    val gameState: GameState,
    val selectedPieceIndex: Int? = null,
    val currentShape: List<Cell>? = null,
    val ghostCells: List<Cell> = emptyList(),
    val ghostValid: Boolean = true,
    val previewRotation: Int = 0,
    val previewFlip: Boolean = false,
    val isAiThinking: Boolean = false,
    val showGameOver: Boolean = false,
    val showModeSelection: Boolean = true,
    val showHistoryStats: Boolean = false,
    val showProfiles: Boolean = false,
    val showResumeDialog: Boolean = false,
    val showSettings: Boolean = false,
    val showHelp: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val elapsedSeconds: Int = 0,
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val history: List<HistoryEntry> = emptyList(),
    val stats: GameStats = GameStats()
)

sealed class GameEvent {
    data object PiecePlaced : GameEvent()
    data object InvalidPlacement : GameEvent()
    data object TurnAdvanced : GameEvent()
    data object GameOver : GameEvent()
    data object GameSaved : GameEvent()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }
    private val aiEngine = AiEngine()
    private val gameRepo = GameRepository(application)
    private val profileRepo = ProfileRepository(application)

    private val _uiState: MutableStateFlow<GameUiState>
    val uiState: StateFlow<GameUiState>

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var session = LocalSession(GameMode.SOLO)
    private var aiJob: Job? = null
    private var timerJob: Job? = null

    init {
        val initialState = GameEngine.createInitialState(GameMode.SOLO)
        _uiState = MutableStateFlow(
            GameUiState(gameState = initialState, showModeSelection = true)
        )
        uiState = _uiState.asStateFlow()

        viewModelScope.launch {
            loadProfiles()
            if (gameRepo.hasSavedGame()) {
                _uiState.value = _uiState.value.copy(showResumeDialog = true)
            }
        }
    }

    // --- Profile Management ---

    private suspend fun loadProfiles() {
        val profiles = profileRepo.getProfiles()
        val active = profiles.firstOrNull { it.active }
        val history = active?.history ?: emptyList()
        val stats = ProfileRepository.calculateStats(history)
        _uiState.value = _uiState.value.copy(
            profiles = profiles,
            activeProfile = active,
            history = history,
            stats = stats
        )
    }

    fun showProfiles() {
        _uiState.value = _uiState.value.copy(showProfiles = true)
    }

    fun dismissProfiles() {
        _uiState.value = _uiState.value.copy(showProfiles = false)
    }

    fun setActiveProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepo.setActiveProfile(profileId)
            loadProfiles()
        }
    }

    fun addProfile(name: String, colorIndex: Int, avatarStyle: String) {
        viewModelScope.launch {
            val profile = Profile(
                id = System.currentTimeMillis(),
                name = name,
                colorIndex = colorIndex,
                avatarStyle = avatarStyle,
                avatarSeed = System.currentTimeMillis().toString()
            )
            profileRepo.addProfile(profile)
            loadProfiles()
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepo.deleteProfile(profileId)
            loadProfiles()
        }
    }

    // --- History & Stats ---

    fun showHistoryStats() {
        _uiState.value = _uiState.value.copy(showHistoryStats = true)
    }

    fun dismissHistoryStats() {
        _uiState.value = _uiState.value.copy(showHistoryStats = false)
    }

    private fun recordGameResult() {
        val state = _uiState.value.gameState
        val active = _uiState.value.activeProfile ?: return
        val rankings = Scoring.calculateRankings(state)

        // Find the human player's ranking
        val humanRanking = rankings.firstOrNull { !it.isSharedColor && it.playerIndex == 0 }
            ?: rankings.firstOrNull { !it.isSharedColor }
            ?: return

        val entry = HistoryEntry(
            date = java.time.Instant.now().toString(),
            rank = humanRanking.rank,
            score = humanRanking.score,
            difficulty = state.difficulty.level,
            gameMode = state.gameMode,
            time = _uiState.value.elapsedSeconds,
            scores = state.players.map { player ->
                PlayerScore(
                    name = player.name,
                    score = player.score,
                    colorIndex = player.index
                )
            }
        )

        viewModelScope.launch {
            profileRepo.recordGameHistory(active.id, entry)
            loadProfiles()
        }
    }

    // --- Save/Load ---

    fun saveGame() {
        viewModelScope.launch {
            gameRepo.saveGame(_uiState.value.gameState)
            _events.tryEmit(GameEvent.GameSaved)
        }
    }

    fun resumeSavedGame() {
        viewModelScope.launch {
            val state = gameRepo.loadGame()
            if (state != null) {
                _uiState.value = _uiState.value.copy(
                    gameState = state,
                    showModeSelection = false,
                    showResumeDialog = false,
                    elapsedSeconds = state.elapsedTimeSeconds
                )
                startTimer()
                checkAndPlayAiTurn()
                gameRepo.deleteSavedGame()
            } else {
                _uiState.value = _uiState.value.copy(showResumeDialog = false)
            }
        }
    }

    fun dismissResumeDialog() {
        viewModelScope.launch {
            gameRepo.deleteSavedGame()
            _uiState.value = _uiState.value.copy(showResumeDialog = false)
        }
    }

    // --- Game Control ---

    fun startNewGame(
        gameMode: GameMode = GameMode.SOLO,
        difficulty: Difficulty = Difficulty.MEDIUM,
        slotConfigs: List<PlayerSlotConfig>? = null
    ) {
        aiJob?.cancel()
        timerJob?.cancel()
        aiEngine.resetStates()
        session = LocalSession(gameMode)
        val state = GameEngine.createInitialState(
            gameMode = gameMode,
            difficulty = difficulty,
            slotConfigs = slotConfigs
        )
        _uiState.value = _uiState.value.copy(
            gameState = state,
            showModeSelection = false,
            showResumeDialog = false,
            selectedPieceIndex = null,
            currentShape = null,
            ghostCells = emptyList(),
            previewRotation = 0,
            previewFlip = false,
            isAiThinking = false,
            showGameOver = false,
            elapsedSeconds = 0
        )
        startTimer()
        checkAndPlayAiTurn()
    }

    fun showModeSelection() {
        aiJob?.cancel()
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(showModeSelection = true)
    }

    fun selectPiece(pieceIndex: Int) {
        val current = _uiState.value
        if (current.isAiThinking || current.gameState.isGameOver) return
        if (!isCurrentTurnLocalHuman()) return
        val player = current.gameState.players[current.gameState.currentPlayerIndex]
        if (player.usedPieces[pieceIndex]) return

        if (current.selectedPieceIndex == pieceIndex) {
            deselectPiece()
            return
        }

        val piece = Pieces.ALL[pieceIndex]
        val shape = PieceTransforms.applyTransform(piece.cells, 0, false)
        _uiState.value = current.copy(
            selectedPieceIndex = pieceIndex,
            currentShape = shape,
            previewRotation = 0,
            previewFlip = false,
            ghostCells = emptyList()
        )
    }

    fun deselectPiece() {
        _uiState.value = _uiState.value.copy(
            selectedPieceIndex = null,
            currentShape = null,
            ghostCells = emptyList(),
            previewRotation = 0,
            previewFlip = false
        )
    }

    fun rotateCW() {
        val current = _uiState.value
        val pieceIndex = current.selectedPieceIndex ?: return
        if (current.isAiThinking) return
        val newRotation = (current.previewRotation + 1) % 4
        val shape = PieceTransforms.applyTransform(
            Pieces.ALL[pieceIndex].cells, newRotation, current.previewFlip
        )
        _uiState.value = current.copy(
            previewRotation = newRotation,
            currentShape = shape,
            ghostCells = emptyList()
        )
    }

    fun rotateCCW() {
        val current = _uiState.value
        val pieceIndex = current.selectedPieceIndex ?: return
        if (current.isAiThinking) return
        val newRotation = (current.previewRotation + 3) % 4
        val shape = PieceTransforms.applyTransform(
            Pieces.ALL[pieceIndex].cells, newRotation, current.previewFlip
        )
        _uiState.value = current.copy(
            previewRotation = newRotation,
            currentShape = shape,
            ghostCells = emptyList()
        )
    }

    fun flipH() {
        val current = _uiState.value
        val pieceIndex = current.selectedPieceIndex ?: return
        if (current.isAiThinking) return
        val newFlip = !current.previewFlip
        val shape = PieceTransforms.applyTransform(
            Pieces.ALL[pieceIndex].cells, current.previewRotation, newFlip
        )
        _uiState.value = current.copy(
            previewFlip = newFlip,
            currentShape = shape,
            ghostCells = emptyList()
        )
    }

    fun updateGhost(anchorCell: Cell?) {
        val current = _uiState.value
        if (anchorCell == null || current.isAiThinking) {
            _uiState.value = current.copy(ghostCells = emptyList())
            return
        }
        val shape = current.currentShape ?: return
        val state = current.gameState

        val midRow = shape.minOf { it.row } + (shape.maxOf { it.row } - shape.minOf { it.row }) / 2
        val midCol = shape.minOf { it.col } + (shape.maxOf { it.col } - shape.minOf { it.col }) / 2

        val placed = shape.map {
            Cell(anchorCell.row + it.row - midRow, anchorCell.col + it.col - midCol)
        }
        val valid = PlacementValidator.isValidPlacement(
            state, state.currentPlayerIndex, placed
        )
        _uiState.value = current.copy(
            ghostCells = placed,
            ghostValid = valid
        )
    }

    fun attemptPlacement(anchorCell: Cell) {
        val current = _uiState.value
        val pieceIndex = current.selectedPieceIndex ?: return
        val shape = current.currentShape ?: return
        val state = current.gameState

        if (state.isGameOver || current.isAiThinking) return
        if (!isCurrentTurnLocalHuman()) return

        val midRow = shape.minOf { it.row } + (shape.maxOf { it.row } - shape.minOf { it.row }) / 2
        val midCol = shape.minOf { it.col } + (shape.maxOf { it.col } - shape.minOf { it.col }) / 2

        val placed = shape.map {
            Cell(anchorCell.row + it.row - midRow, anchorCell.col + it.col - midCol)
        }
        val move = Move(
            playerIndex = state.currentPlayerIndex,
            pieceIndex = pieceIndex,
            cells = placed,
            rotations = current.previewRotation,
            flipped = current.previewFlip
        )

        if (!PlacementValidator.isValidPlacement(state, move.playerIndex, move.cells)) {
            _events.tryEmit(GameEvent.InvalidPlacement)
            return
        }

        val newState = GameEngine.applyMove(state, move)
        _uiState.value = current.copy(
            gameState = newState,
            selectedPieceIndex = null,
            currentShape = null,
            ghostCells = emptyList(),
            previewRotation = 0,
            previewFlip = false
        )
        _events.tryEmit(GameEvent.PiecePlaced)

        if (newState.isGameOver) {
            onGameOver()
        } else {
            _events.tryEmit(GameEvent.TurnAdvanced)
            checkAndPlayAiTurn()
        }
    }

    fun passTurn() {
        val current = _uiState.value
        val state = current.gameState
        if (state.isGameOver || current.isAiThinking) return
        if (!isCurrentTurnLocalHuman()) return

        val newState = GameEngine.applyPass(state, state.currentPlayerIndex)
        _uiState.value = current.copy(
            gameState = newState,
            selectedPieceIndex = null,
            currentShape = null,
            ghostCells = emptyList()
        )

        if (newState.isGameOver) {
            onGameOver()
        } else {
            checkAndPlayAiTurn()
        }
    }

    fun dismissGameOver() {
        _uiState.value = _uiState.value.copy(showGameOver = false)
    }

    fun showSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }

    fun dismissSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }

    fun showHelp() {
        _uiState.value = _uiState.value.copy(showHelp = true, showSettings = false)
    }

    fun dismissHelp() {
        _uiState.value = _uiState.value.copy(showHelp = false)
    }

    fun toggleSound() {
        val current = _uiState.value
        _uiState.value = current.copy(soundEnabled = !current.soundEnabled)
        viewModelScope.launch {
            val settings = profileRepo.getSettings()
            profileRepo.saveSettings(settings.copy(soundEnabled = !current.soundEnabled))
        }
    }

    fun toggleHaptics() {
        val current = _uiState.value
        _uiState.value = current.copy(hapticsEnabled = !current.hapticsEnabled)
        viewModelScope.launch {
            val settings = profileRepo.getSettings()
            profileRepo.saveSettings(settings.copy(hapticsEnabled = !current.hapticsEnabled))
        }
    }

    // --- Turn Control ---

    private fun isCurrentTurnLocalHuman(): Boolean {
        val state = _uiState.value.gameState
        val player = state.players[state.currentPlayerIndex]
        return !player.isAi
    }

    // --- AI Turn Management ---

    private fun checkAndPlayAiTurn() {
        val state = _uiState.value.gameState
        if (state.isGameOver) return

        val currentPlayer = state.players[state.currentPlayerIndex]

        if (!currentPlayer.isAi && !GameEngine.hasValidMove(state, currentPlayer.index)) {
            viewModelScope.launch {
                delay(GameConstants.HUMAN_AUTO_PASS_DELAY_MS)
                val current = _uiState.value
                if (!current.gameState.isGameOver &&
                    current.gameState.currentPlayerIndex == currentPlayer.index
                ) {
                    val newState = GameEngine.applyPass(current.gameState, currentPlayer.index)
                    _uiState.value = current.copy(gameState = newState)
                    if (newState.isGameOver) onGameOver() else checkAndPlayAiTurn()
                }
            }
            return
        }

        if (!currentPlayer.isAi) return

        _uiState.value = _uiState.value.copy(isAiThinking = true)

        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            val delayMs = GameConstants.AI_TURN_DELAY_MIN_MS +
                (Math.random() * GameConstants.AI_TURN_DELAY_RANGE_MS).toLong()
            delay(delayMs)

            val currentState = _uiState.value.gameState
            if (currentState.isGameOver || !isActive) return@launch

            if (!GameEngine.hasValidMove(currentState, currentState.currentPlayerIndex)) {
                val newState = GameEngine.applyPass(currentState, currentState.currentPlayerIndex)
                _uiState.value = _uiState.value.copy(gameState = newState, isAiThinking = false)
                if (newState.isGameOver) onGameOver() else checkAndPlayAiTurn()
                return@launch
            }

            val move = withContext(Dispatchers.Default) {
                aiEngine.computeMove(currentState)
            }

            if (!isActive) return@launch

            if (move != null) {
                try {
                    val newState = GameEngine.applyMove(currentState, move)
                    _uiState.value = _uiState.value.copy(gameState = newState, isAiThinking = false)
                    _events.tryEmit(GameEvent.PiecePlaced)

                    if (newState.isGameOver) {
                        onGameOver()
                    } else {
                        delay(GameConstants.TURN_ADVANCE_DELAY_MS)
                        checkAndPlayAiTurn()
                    }
                } catch (_: IllegalArgumentException) {
                    val newState = GameEngine.applyPass(currentState, currentState.currentPlayerIndex)
                    _uiState.value = _uiState.value.copy(gameState = newState, isAiThinking = false)
                    if (newState.isGameOver) onGameOver() else checkAndPlayAiTurn()
                }
            } else {
                val newState = GameEngine.applyPass(currentState, currentState.currentPlayerIndex)
                _uiState.value = _uiState.value.copy(gameState = newState, isAiThinking = false)
                if (newState.isGameOver) onGameOver() else checkAndPlayAiTurn()
            }
        }
    }

    private fun onGameOver() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(showGameOver = true, isAiThinking = false)
        _events.tryEmit(GameEvent.GameOver)
        recordGameResult()
        viewModelScope.launch { gameRepo.deleteSavedGame() }
    }

    // --- Timer ---

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value
                if (!current.gameState.isGameOver) {
                    _uiState.value = current.copy(
                        elapsedSeconds = current.elapsedSeconds + 1,
                        gameState = current.gameState.copy(
                            elapsedTimeSeconds = current.elapsedSeconds + 1
                        )
                    )
                }
            }
        }
    }

    fun getCurrentTurnLabel(): String {
        val state = _uiState.value.gameState
        val player = state.players[state.currentPlayerIndex]
        return when {
            state.isGameOver -> "Game Over!"
            player.isAi -> "${player.name} is thinking..."
            player.isSharedColor -> {
                val controllerIdx = state.getControllingHumanIndex(player.index)
                val controllerName = state.players.getOrNull(controllerIdx)?.name ?: "?"
                "$controllerName plays Shared color"
            }
            state.isPassAndPlay && state.gameMode != GameMode.SOLO -> "${player.name}'s turn"
            else -> "Your turn!"
        }
    }

    fun loadMultiplayerGame(state: GameState) {
        aiJob?.cancel()
        timerJob?.cancel()
        aiEngine.resetStates()
        _uiState.value = _uiState.value.copy(
            gameState = state,
            showModeSelection = false,
            showResumeDialog = false,
            selectedPieceIndex = null,
            currentShape = null,
            ghostCells = emptyList(),
            previewRotation = 0,
            previewFlip = false,
            isAiThinking = false,
            showGameOver = false,
            elapsedSeconds = 0
        )
        startTimer()
        checkAndPlayAiTurn()
    }

    fun getRankings(): List<Scoring.PlayerRanking> =
        Scoring.calculateRankings(_uiState.value.gameState)

    override fun onCleared() {
        super.onCleared()
        aiJob?.cancel()
        timerJob?.cancel()
    }
}
