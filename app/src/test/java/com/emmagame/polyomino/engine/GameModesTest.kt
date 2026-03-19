package com.emmagame.polyomino.engine

import com.emmagame.polyomino.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class GameModesTest {

    // --- Duo Mode (14×14) ---

    @Test
    fun `duo mode creates 14x14 board`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_DUO)
        assertEquals(14, state.board.size)
        assertEquals(196, state.board.cells.size)
    }

    @Test
    fun `duo mode has 2 players`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_DUO)
        assertEquals(2, state.players.size)
    }

    @Test
    fun `duo mode has offset start positions`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_DUO)
        assertEquals(Cell(4, 4), state.startPositions[0])
        assertEquals(Cell(9, 9), state.startPositions[1])
    }

    @Test
    fun `duo mode first piece must cover start position`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_DUO)
        val start = state.startPositions[0]
        assertTrue(PlacementValidator.isValidPlacement(state, 0, listOf(start)))
        assertFalse(PlacementValidator.isValidPlacement(state, 0, listOf(Cell(0, 0))))
    }

    @Test
    fun `duo mode game ends when both pass`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_DUO)
        val s1 = GameEngine.applyPass(state, 0)
        val s2 = GameEngine.applyPass(s1, 1)
        assertTrue(s2.isGameOver)
    }

    // --- Two-Player Standard (20×20, 2 colors each) ---

    @Test
    fun `two player standard has 4 colors`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_STANDARD)
        assertEquals(4, state.players.size)
    }

    @Test
    fun `two player standard assigns controlledBy`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_STANDARD)
        // Player 0,2 controlled by human 0; Player 1,3 controlled by human 1
        assertEquals(0, state.players[0].controlledBy)
        assertEquals(1, state.players[1].controlledBy)
        assertEquals(0, state.players[2].controlledBy)
        assertEquals(1, state.players[3].controlledBy)
    }

    @Test
    fun `two player standard alternates colors 0 1 2 3`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_STANDARD)
        assertEquals(0, state.currentPlayerIndex)

        val start0 = state.startPositions[0]
        val move0 = Move(0, 0, listOf(start0), 0, false)
        val s1 = GameEngine.applyMove(state, move0)
        assertEquals(1, s1.currentPlayerIndex)
    }

    @Test
    fun `two player standard uses 20x20 board`() {
        val state = GameEngine.createInitialState(GameMode.TWO_PLAYER_STANDARD)
        assertEquals(20, state.board.size)
    }

    // --- Three-Player Mode ---

    @Test
    fun `three player mode has 4 players with shared color`() {
        val state = GameEngine.createInitialState(GameMode.THREE_PLAYER)
        assertEquals(4, state.players.size)
        assertEquals(3, state.sharedColorIndex)
        assertTrue(state.players[3].isSharedColor)
    }

    @Test
    fun `three player shared color is not ranked`() {
        val state = GameEngine.createInitialState(GameMode.THREE_PLAYER)
        val rankings = Scoring.calculateRankings(state)
        val rankedPlayers = rankings.filter { !it.isSharedColor }
        assertEquals(3, rankedPlayers.size)
        val sharedPlayers = rankings.filter { it.isSharedColor }
        assertEquals(1, sharedPlayers.size)
        assertEquals(0, sharedPlayers[0].rank)
    }

    @Test
    fun `three player shared color turn counter increments`() {
        val state = GameEngine.createInitialState(GameMode.THREE_PLAYER)
        assertEquals(0, state.sharedColorTurnCounter)

        // Play through to shared color's turn (index 3)
        var s = state
        for (i in 0 until 3) {
            val start = s.startPositions[s.currentPlayerIndex]
            val move = Move(s.currentPlayerIndex, 0, listOf(start), 0, false)
            s = GameEngine.applyMove(s, move)
        }

        // Now it should be player 3's turn (shared)
        assertEquals(3, s.currentPlayerIndex)
        assertTrue(s.players[3].isSharedColor)

        val start3 = s.startPositions[3]
        val move3 = Move(3, 0, listOf(start3), 0, false)
        s = GameEngine.applyMove(s, move3)

        assertEquals(1, s.sharedColorTurnCounter)
    }

    @Test
    fun `three player getControllingHumanIndex rotates among real players`() {
        val state = GameEngine.createInitialState(GameMode.THREE_PLAYER)
        // Counter 0 -> player 0 controls shared
        assertEquals(0, state.getControllingHumanIndex(3))
        // Counter 1 -> player 1
        val s1 = state.copy(sharedColorTurnCounter = 1)
        assertEquals(1, s1.getControllingHumanIndex(3))
        // Counter 2 -> player 2
        val s2 = state.copy(sharedColorTurnCounter = 2)
        assertEquals(2, s2.getControllingHumanIndex(3))
        // Counter 3 -> player 0 again
        val s3 = state.copy(sharedColorTurnCounter = 3)
        assertEquals(0, s3.getControllingHumanIndex(3))
    }

    // --- Four-Player Mode ---

    @Test
    fun `four player mode has 4 human players`() {
        val state = GameEngine.createInitialState(GameMode.FOUR_PLAYER)
        assertEquals(4, state.players.size)
        assertTrue(state.players.none { it.isAi })
    }

    @Test
    fun `four player mode uses standard corners`() {
        val state = GameEngine.createInitialState(GameMode.FOUR_PLAYER)
        assertEquals(GameConstants.STANDARD_CORNERS, state.startPositions)
    }

    // --- Mixed Modes (human + AI) ---

    @Test
    fun `four player mode with mixed AI slots`() {
        val slots = listOf(
            PlayerSlotConfig(isAi = false, name = "Human 1"),
            PlayerSlotConfig(isAi = true, name = "Bot 1"),
            PlayerSlotConfig(isAi = false, name = "Human 2"),
            PlayerSlotConfig(isAi = true, name = "Bot 2")
        )
        val state = GameEngine.createInitialState(
            GameMode.FOUR_PLAYER,
            slotConfigs = slots
        )
        assertFalse(state.players[0].isAi)
        assertTrue(state.players[1].isAi)
        assertFalse(state.players[2].isAi)
        assertTrue(state.players[3].isAi)
        assertEquals("Human 1", state.players[0].name)
        assertEquals("Bot 1", state.players[1].name)
    }

    @Test
    fun `duo mode with one AI player`() {
        val slots = listOf(
            PlayerSlotConfig(isAi = false, name = "Human"),
            PlayerSlotConfig(isAi = true, name = "AI")
        )
        val state = GameEngine.createInitialState(
            GameMode.TWO_PLAYER_DUO,
            slotConfigs = slots
        )
        assertFalse(state.players[0].isAi)
        assertTrue(state.players[1].isAi)
    }

    // --- Serialization ---

    @Test
    fun `all game modes serialize and deserialize`() {
        val json = Json { prettyPrint = false }
        for (mode in GameMode.entries) {
            val state = GameEngine.createInitialState(mode)
            val serialized = json.encodeToString(state)
            val deserialized = json.decodeFromString<GameState>(serialized)
            assertEquals(state.board.size, deserialized.board.size)
            assertEquals(state.players.size, deserialized.players.size)
            assertEquals(state.gameMode, deserialized.gameMode)
            assertEquals(state.sharedColorIndex, deserialized.sharedColorIndex)
        }
    }

    @Test
    fun `three player mode with moves serializes correctly`() {
        val json = Json { prettyPrint = false }
        var state = GameEngine.createInitialState(GameMode.THREE_PLAYER)
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        state = GameEngine.applyMove(state, move)

        val serialized = json.encodeToString(state)
        val deserialized = json.decodeFromString<GameState>(serialized)
        assertEquals(state.sharedColorTurnCounter, deserialized.sharedColorTurnCounter)
        assertTrue(deserialized.players[3].isSharedColor)
    }
}
