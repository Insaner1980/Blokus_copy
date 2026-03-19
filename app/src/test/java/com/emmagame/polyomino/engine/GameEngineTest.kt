package com.emmagame.polyomino.engine

import com.emmagame.polyomino.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var state: GameState

    @Before
    fun setup() {
        state = GameEngine.createInitialState(GameMode.SOLO)
    }

    @Test
    fun `createInitialState sets correct board size`() {
        assertEquals(20, state.board.size)
        assertEquals(400, state.board.cells.size)
    }

    @Test
    fun `createInitialState has 4 players`() {
        assertEquals(4, state.players.size)
    }

    @Test
    fun `createInitialState all cells are empty`() {
        assertTrue(state.board.cells.all { it == Board.EMPTY })
    }

    @Test
    fun `createInitialState player 0 starts`() {
        assertEquals(0, state.currentPlayerIndex)
    }

    @Test
    fun `createInitialState players have correct initial scores`() {
        for (player in state.players) {
            assertEquals(GameConstants.TOTAL_CELLS, player.score)
            assertEquals(0, player.piecesPlaced)
            assertFalse(player.passed)
        }
    }

    @Test
    fun `createInitialState solo mode has correct AI setup`() {
        assertFalse(state.players[0].isAi)
        assertTrue(state.players[1].isAi)
        assertTrue(state.players[2].isAi)
        assertTrue(state.players[3].isAi)
    }

    @Test
    fun `createInitialState duo mode has correct board size`() {
        val duoState = GameEngine.createInitialState(GameMode.TWO_PLAYER_DUO)
        assertEquals(14, duoState.board.size)
        assertEquals(196, duoState.board.cells.size)
        assertEquals(2, duoState.players.size)
    }

    @Test
    fun `applyMove updates board correctly`() {
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val newState = GameEngine.applyMove(state, move)

        assertEquals(0, newState.board.get(start.row, start.col))
    }

    @Test
    fun `applyMove updates player score`() {
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false) // monomino = 1 cell
        val newState = GameEngine.applyMove(state, move)

        assertEquals(GameConstants.TOTAL_CELLS - 1, newState.players[0].score)
        assertEquals(1, newState.players[0].piecesPlaced)
        assertTrue(newState.players[0].usedPieces[0])
    }

    @Test
    fun `applyMove advances turn`() {
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val newState = GameEngine.applyMove(state, move)

        assertEquals(1, newState.currentPlayerIndex)
    }

    @Test
    fun `applyMove resets consecutive passes`() {
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val stateWithPasses = state.copy(consecutivePasses = 2)
        val newState = GameEngine.applyMove(stateWithPasses, move)

        assertEquals(0, newState.consecutivePasses)
    }

    @Test
    fun `applyPass marks player as passed`() {
        val newState = GameEngine.applyPass(state, 0)
        assertTrue(newState.players[0].passed)
    }

    @Test
    fun `applyPass increments consecutive passes`() {
        val newState = GameEngine.applyPass(state, 0)
        assertEquals(1, newState.consecutivePasses)
    }

    @Test
    fun `game ends when all players pass`() {
        var s = state
        for (i in 0 until 4) {
            s = GameEngine.applyPass(s, s.currentPlayerIndex)
        }
        assertTrue(s.isGameOver)
    }

    @Test
    fun `advanceTurn skips passed players`() {
        val passedPlayers = state.players.toMutableList()
        passedPlayers[1] = passedPlayers[1].withPassed()
        val modifiedState = state.copy(players = passedPlayers)

        val start = modifiedState.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val newState = GameEngine.applyMove(modifiedState, move)

        assertEquals(2, newState.currentPlayerIndex)
    }

    @Test
    fun `applyMove with multi-cell piece scores correctly`() {
        // Use domino (piece index 1, 2 cells)
        val start = state.startPositions[0]
        val piece = Pieces.ALL[1] // domino
        val cells = piece.cells.map { Cell(start.row + it.row, start.col + it.col) }

        // Check if placement is valid (domino may need specific orientation)
        // For start at (0,0), domino cells (0,0)+(0,1) should work
        val move = Move(0, 1, cells, 0, false)
        val newState = GameEngine.applyMove(state, move)

        assertEquals(GameConstants.TOTAL_CELLS - 2, newState.players[0].score)
    }

    @Test
    fun `move history records moves`() {
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val newState = GameEngine.applyMove(state, move)

        assertEquals(1, newState.moveHistory.size)
        assertEquals(move, newState.moveHistory[0])
    }

    @Test
    fun `GameState serializes and deserializes`() {
        val json = Json { prettyPrint = false }
        val serialized = json.encodeToString(state)
        val deserialized = json.decodeFromString<GameState>(serialized)

        assertEquals(state.board, deserialized.board)
        assertEquals(state.currentPlayerIndex, deserialized.currentPlayerIndex)
        assertEquals(state.gameMode, deserialized.gameMode)
        assertEquals(state.players.size, deserialized.players.size)
        assertEquals(state.isGameOver, deserialized.isGameOver)
    }

    @Test
    fun `GameState serializes after moves`() {
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val newState = GameEngine.applyMove(state, move)

        val json = Json { prettyPrint = false }
        val serialized = json.encodeToString(newState)
        val deserialized = json.decodeFromString<GameState>(serialized)

        assertEquals(newState.board, deserialized.board)
        assertEquals(newState.moveHistory.size, deserialized.moveHistory.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applyMove on wrong player's turn throws`() {
        val move = Move(1, 0, listOf(state.startPositions[1]), 0, false)
        GameEngine.applyMove(state, move)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `applyMove on game over throws`() {
        val gameOverState = state.copy(isGameOver = true)
        val move = Move(0, 0, listOf(state.startPositions[0]), 0, false)
        GameEngine.applyMove(gameOverState, move)
    }

    @Test
    fun `hasValidMove returns true at game start`() {
        assertTrue(GameEngine.hasValidMove(state, 0))
    }

    @Test
    fun `scoring rankings sort by score ascending`() {
        // Modify players to have different scores
        val players = state.players.toMutableList()
        players[0] = players[0].copy(score = 50)
        players[1] = players[1].copy(score = 30)
        players[2] = players[2].copy(score = 70)
        players[3] = players[3].copy(score = 30)
        val modifiedState = state.copy(players = players)

        val rankings = Scoring.calculateRankings(modifiedState)
        assertEquals(4, rankings.size)
        // Player 1 and 3 tied at 30 (lowest), then player 0 at 50, then player 2 at 70
        assertEquals(1, rankings[0].rank)
        assertEquals(30, rankings[0].score)
        assertTrue(rankings.last().score >= rankings.first().score)
    }
}
