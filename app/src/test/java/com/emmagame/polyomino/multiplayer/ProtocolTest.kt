package com.emmagame.polyomino.multiplayer

import com.emmagame.polyomino.engine.GameEngine
import com.emmagame.polyomino.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ProtocolTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `PlaceMove message serializes and deserializes`() {
        val move = Move(0, 5, listOf(Cell(0, 0), Cell(0, 1)), 1, false)
        val message = GameMessage.PlaceMove(move)
        val serialized = json.encodeToString<GameMessage>(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertTrue(deserialized is GameMessage.PlaceMove)
        assertEquals(move, (deserialized as GameMessage.PlaceMove).move)
    }

    @Test
    fun `Pass message serializes and deserializes`() {
        val message = GameMessage.Pass(2)
        val serialized = json.encodeToString<GameMessage>(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertTrue(deserialized is GameMessage.Pass)
        assertEquals(2, (deserialized as GameMessage.Pass).playerIndex)
    }

    @Test
    fun `FullSync message serializes and deserializes`() {
        val state = GameEngine.createInitialState(GameMode.SOLO)
        val message = GameMessage.FullSync(state)
        val serialized = json.encodeToString<GameMessage>(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertTrue(deserialized is GameMessage.FullSync)
        val syncedState = (deserialized as GameMessage.FullSync).state
        assertEquals(state.board.size, syncedState.board.size)
        assertEquals(state.players.size, syncedState.players.size)
    }

    @Test
    fun `PlayerJoined message serializes and deserializes`() {
        val player = SessionPlayer(1, "TestPlayer", isLocal = false, isAi = false, colorIndex = 1)
        val message = GameMessage.PlayerJoined(player)
        val serialized = json.encodeToString<GameMessage>(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertTrue(deserialized is GameMessage.PlayerJoined)
        assertEquals("TestPlayer", (deserialized as GameMessage.PlayerJoined).player.name)
    }

    @Test
    fun `PlayerLeft message serializes and deserializes`() {
        val message = GameMessage.PlayerLeft(3)
        val serialized = json.encodeToString<GameMessage>(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertTrue(deserialized is GameMessage.PlayerLeft)
        assertEquals(3, (deserialized as GameMessage.PlayerLeft).playerIndex)
    }

    @Test
    fun `GameConfig message serializes and deserializes`() {
        val message = GameMessage.GameConfig(GameMode.FOUR_PLAYER, 4)
        val serialized = json.encodeToString<GameMessage>(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertTrue(deserialized is GameMessage.GameConfig)
        val config = deserialized as GameMessage.GameConfig
        assertEquals(GameMode.FOUR_PLAYER, config.mode)
        assertEquals(4, config.difficulty)
    }

    @Test
    fun `Ping and Pong serialize correctly`() {
        val ping = json.encodeToString<GameMessage>(GameMessage.Ping)
        val pong = json.encodeToString<GameMessage>(GameMessage.Pong)
        assertTrue(json.decodeFromString<GameMessage>(ping) is GameMessage.Ping)
        assertTrue(json.decodeFromString<GameMessage>(pong) is GameMessage.Pong)
    }

    @Test
    fun `SessionPlayer serializes and deserializes`() {
        val player = SessionPlayer(
            index = 2,
            name = "Alice",
            isLocal = false,
            isAi = true,
            colorIndex = 3
        )
        val serialized = json.encodeToString(player)
        val deserialized = json.decodeFromString<SessionPlayer>(serialized)
        assertEquals(player, deserialized)
    }

    @Test
    fun `FullSync with game in progress serializes correctly`() {
        var state = GameEngine.createInitialState(GameMode.FOUR_PLAYER)
        val start = state.startPositions[0]
        val move = Move(0, 0, listOf(start), 0, false)
        state = GameEngine.applyMove(state, move)

        val message = GameMessage.FullSync(state)
        val serialized = json.encodeToString<GameMessage>(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        val syncedState = (deserialized as GameMessage.FullSync).state

        assertEquals(1, syncedState.moveHistory.size)
        assertEquals(1, syncedState.currentPlayerIndex)
        assertEquals(0, syncedState.board.get(start.row, start.col))
    }

    @Test
    fun `all message types have distinct serialization`() {
        val messages = listOf(
            GameMessage.PlaceMove(Move(0, 0, listOf(Cell(0, 0)), 0, false)),
            GameMessage.Pass(0),
            GameMessage.FullSync(GameEngine.createInitialState(GameMode.SOLO)),
            GameMessage.PlayerJoined(SessionPlayer(0, "P", true, false, 0)),
            GameMessage.PlayerLeft(0),
            GameMessage.GameConfig(GameMode.SOLO, 3),
            GameMessage.Ping,
            GameMessage.Pong
        )

        val serialized = messages.map { json.encodeToString<GameMessage>(it) }
        // All should be unique
        assertEquals(serialized.size, serialized.toSet().size)
    }
}
