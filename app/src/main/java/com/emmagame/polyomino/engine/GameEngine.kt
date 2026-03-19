package com.emmagame.polyomino.engine

import com.emmagame.polyomino.model.*

data class PlayerSlotConfig(
    val isAi: Boolean = false,
    val name: String = ""
)

object GameEngine {

    fun createInitialState(
        gameMode: GameMode,
        difficulty: Difficulty = Difficulty.MEDIUM,
        playerNames: List<String> = GameConstants.PLAYER_NAMES,
        humanPlayerIndex: Int = 0,
        slotConfigs: List<PlayerSlotConfig>? = null,
        isPassAndPlay: Boolean = false
    ): GameState {
        val boardSize = gameMode.boardSize
        val board = Board.create(boardSize)
        val startPositions = gameMode.getStartPositions()

        val players: List<Player>
        var sharedColorIndex = -1

        when (gameMode) {
            GameMode.SOLO -> {
                players = List(4) { i ->
                    val config = slotConfigs?.getOrNull(i)
                    Player(
                        index = i,
                        name = config?.name?.ifBlank { null }
                            ?: if (i < playerNames.size) playerNames[i] else "Player ${i + 1}",
                        isAi = config?.isAi ?: (i != humanPlayerIndex),
                        isLocal = true
                    )
                }
            }

            GameMode.TWO_PLAYER_STANDARD -> {
                // 4 colors, but only 2 human players
                // Player 1 controls colors 0 and 2 (opposite corners)
                // Player 2 controls colors 1 and 3 (opposite corners)
                players = List(4) { i ->
                    val config = slotConfigs?.getOrNull(i)
                    val humanController = if (i % 2 == 0) 0 else 1
                    val isAi = config?.isAi ?: false
                    Player(
                        index = i,
                        name = config?.name?.ifBlank { null }
                            ?: GameConstants.PLAYER_COLORS[i],
                        isAi = isAi,
                        isLocal = true,
                        controlledBy = if (!isAi) humanController else -1
                    )
                }
            }

            GameMode.TWO_PLAYER_DUO -> {
                players = List(2) { i ->
                    val config = slotConfigs?.getOrNull(i)
                    Player(
                        index = i,
                        name = config?.name?.ifBlank { null }
                            ?: if (i < playerNames.size) playerNames[i] else "Player ${i + 1}",
                        isAi = config?.isAi ?: false,
                        isLocal = true
                    )
                }
            }

            GameMode.THREE_PLAYER -> {
                // 3 real players + 1 shared color (index 3)
                sharedColorIndex = 3
                players = List(4) { i ->
                    val config = slotConfigs?.getOrNull(i)
                    if (i == 3) {
                        Player(
                            index = 3,
                            name = "Shared",
                            isAi = false,
                            isLocal = true,
                            isSharedColor = true
                        )
                    } else {
                        Player(
                            index = i,
                            name = config?.name?.ifBlank { null }
                                ?: if (i < playerNames.size) playerNames[i] else "Player ${i + 1}",
                            isAi = config?.isAi ?: false,
                            isLocal = true
                        )
                    }
                }
            }

            GameMode.FOUR_PLAYER -> {
                players = List(4) { i ->
                    val config = slotConfigs?.getOrNull(i)
                    Player(
                        index = i,
                        name = config?.name?.ifBlank { null }
                            ?: if (i < playerNames.size) playerNames[i] else "Player ${i + 1}",
                        isAi = config?.isAi ?: false,
                        isLocal = true
                    )
                }
            }
        }

        return GameState(
            board = board,
            players = players,
            currentPlayerIndex = 0,
            gameMode = gameMode,
            difficulty = difficulty,
            startPositions = startPositions,
            sharedColorIndex = sharedColorIndex,
            isPassAndPlay = isPassAndPlay || gameMode != GameMode.SOLO
        )
    }

    fun applyMove(state: GameState, move: Move): GameState {
        require(!state.isGameOver) { "Game is already over" }
        require(move.playerIndex == state.currentPlayerIndex) {
            "Not player ${move.playerIndex}'s turn, current is ${state.currentPlayerIndex}"
        }
        require(PlacementValidator.isValidPlacement(state, move.playerIndex, move.cells)) {
            "Invalid placement"
        }

        val player = state.players[move.playerIndex]
        require(!player.usedPieces[move.pieceIndex]) {
            "Piece ${move.pieceIndex} already used"
        }

        val newBoard = state.board.withPlacements(move.cells, move.playerIndex)
        val newPlayer = player.withPiecePlaced(move.pieceIndex, move.cells.size)
        val newPlayers = state.players.toMutableList()
        newPlayers[move.playerIndex] = newPlayer

        // Update shared color turn counter
        val newSharedCounter = if (player.isSharedColor) {
            state.sharedColorTurnCounter + 1
        } else state.sharedColorTurnCounter

        val newState = state.copy(
            board = newBoard,
            players = newPlayers,
            moveHistory = state.moveHistory + move,
            consecutivePasses = 0,
            sharedColorTurnCounter = newSharedCounter
        )

        return advanceTurn(newState)
    }

    fun applyPass(state: GameState, playerIndex: Int): GameState {
        require(!state.isGameOver) { "Game is already over" }
        require(playerIndex == state.currentPlayerIndex) {
            "Not player $playerIndex's turn"
        }

        val newPlayers = state.players.toMutableList()
        newPlayers[playerIndex] = state.players[playerIndex].withPassed()

        val newConsecutive = state.consecutivePasses + 1

        val newSharedCounter = if (state.players[playerIndex].isSharedColor) {
            state.sharedColorTurnCounter + 1
        } else state.sharedColorTurnCounter

        val newState = state.copy(
            players = newPlayers,
            consecutivePasses = newConsecutive,
            sharedColorTurnCounter = newSharedCounter
        )

        if (isGameOver(newState)) {
            return newState.copy(isGameOver = true)
        }

        return advanceTurn(newState)
    }

    fun advanceTurn(state: GameState): GameState {
        if (isGameOver(state)) {
            return state.copy(isGameOver = true)
        }

        var next = (state.currentPlayerIndex + 1) % state.players.size
        var attempts = 0
        while (state.players[next].passed && attempts < state.players.size) {
            next = (next + 1) % state.players.size
            attempts++
        }

        if (attempts >= state.players.size) {
            return state.copy(isGameOver = true)
        }

        return state.copy(currentPlayerIndex = next)
    }

    fun hasValidMove(state: GameState, playerIndex: Int): Boolean {
        val player = state.players[playerIndex]
        if (player.passed) return false

        val cornerPositions = PlacementValidator.getCornerPositions(state, playerIndex)
        if (cornerPositions.isEmpty()) return false

        for (pieceIdx in Pieces.ALL.indices) {
            if (player.usedPieces[pieceIdx]) continue

            val piece = Pieces.ALL[pieceIdx]
            val orientations = PieceTransforms.getAllOrientations(piece.cells)

            for (orientation in orientations) {
                for (corner in cornerPositions) {
                    for (anchor in orientation) {
                        val placed = orientation.map { cell ->
                            Cell(
                                corner.row + cell.row - anchor.row,
                                corner.col + cell.col - anchor.col
                            )
                        }
                        if (PlacementValidator.isValidPlacement(state, playerIndex, placed)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun isGameOver(state: GameState): Boolean {
        if (state.consecutivePasses >= state.players.size) return true
        return state.players.all { it.passed }
    }

    fun isCurrentPlayerHumanControlled(state: GameState): Boolean {
        val player = state.players[state.currentPlayerIndex]
        if (player.isAi) return false
        if (player.isSharedColor) {
            val controllerIdx = state.getControllingHumanIndex(player.index)
            return controllerIdx >= 0 && !state.players.getOrNull(controllerIdx)?.isAi.let { it == true }
        }
        return true
    }
}
