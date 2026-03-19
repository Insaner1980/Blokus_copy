package com.emmagame.polyomino.ai

import com.emmagame.polyomino.engine.GameEngine
import com.emmagame.polyomino.engine.PlacementValidator
import com.emmagame.polyomino.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class AiEngineTest {

    private lateinit var state: GameState
    private lateinit var aiEngine: AiEngine

    @Before
    fun setup() {
        state = GameEngine.createInitialState(GameMode.SOLO)
        aiEngine = AiEngine(random = Random(42))
    }

    @Test
    fun `computeMove returns valid move for first AI turn`() {
        // Player 0 plays first
        val start = state.startPositions[0]
        val move0 = Move(0, 0, listOf(start), 0, false)
        val stateAfter0 = GameEngine.applyMove(state, move0)

        // Player 1 is AI, should compute a valid move
        val aiMove = aiEngine.computeMove(stateAfter0)
        assertNotNull(aiMove)
        assertTrue(
            PlacementValidator.isValidPlacement(stateAfter0, aiMove!!.playerIndex, aiMove.cells)
        )
    }

    @Test
    fun `computeMove returns null for passed player`() {
        val passedPlayers = state.players.toMutableList()
        passedPlayers[0] = passedPlayers[0].withPassed()
        val passedState = state.copy(players = passedPlayers)

        val move = aiEngine.computeMove(passedState)
        assertNull(move)
    }

    @Test
    fun `AI plays valid moves across multiple turns`() {
        var currentState = state
        // Play first piece for player 0 (human)
        val start = currentState.startPositions[0]
        val humanMove = Move(0, 0, listOf(start), 0, false)
        currentState = GameEngine.applyMove(currentState, humanMove)

        // Let AI play several turns
        var aiTurns = 0
        while (currentState.players[currentState.currentPlayerIndex].isAi && aiTurns < 10) {
            val move = aiEngine.computeMove(currentState)
            if (move == null) {
                currentState = GameEngine.applyPass(currentState, currentState.currentPlayerIndex)
            } else {
                assertTrue(
                    "AI move should be valid",
                    PlacementValidator.isValidPlacement(currentState, move.playerIndex, move.cells)
                )
                currentState = GameEngine.applyMove(currentState, move)
            }
            aiTurns++
        }

        assertTrue("AI should have played at least 1 turn", aiTurns >= 1)
    }

    @Test
    fun `AI works with all difficulty levels`() {
        for (difficulty in Difficulty.entries) {
            val diffState = GameEngine.createInitialState(GameMode.SOLO, difficulty)
            val start = diffState.startPositions[0]
            val humanMove = Move(0, 0, listOf(start), 0, false)
            val afterHuman = GameEngine.applyMove(diffState, humanMove)

            val engine = AiEngine(random = Random(42))
            val move = engine.computeMove(afterHuman)
            assertNotNull("AI should find a move at difficulty ${difficulty.displayName}", move)
        }
    }

    @Test
    fun `personality assignment is consistent`() {
        assertEquals(AiPersonality.EXPANSIONIST, AiPersonality.forPlayerIndex(0))
        assertEquals(AiPersonality.AGGRESSIVE, AiPersonality.forPlayerIndex(1))
        assertEquals(AiPersonality.DEFENSIVE, AiPersonality.forPlayerIndex(2))
        assertEquals(AiPersonality.EXPANSIONIST, AiPersonality.forPlayerIndex(3))
    }

    @Test
    fun `territory estimation returns non-negative`() {
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val newState = GameEngine.applyMove(state, move)

        val territory = Territory.estimateTerritory(newState, 0)
        assertTrue("Territory should be non-negative", territory >= 0)
    }

    @Test
    fun `territory estimation increases after placing pieces`() {
        // Empty board = no territory (no corners to flood from)
        val emptyTerritory = Territory.estimateTerritory(state, 0)

        // After placing a piece, should have some territory
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        val newState = GameEngine.applyMove(state, move)
        val afterTerritory = Territory.estimateTerritory(newState, 0)

        assertTrue("Territory after placement should be > 0", afterTerritory > 0)
    }
}
