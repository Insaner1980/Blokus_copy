package com.emmagame.polyomino.multiplayer

import com.emmagame.polyomino.model.GameMode
import com.emmagame.polyomino.model.GameState
import com.emmagame.polyomino.model.Move
import kotlinx.serialization.Serializable

@Serializable
sealed class GameMessage {
    @Serializable
    data class PlaceMove(val move: Move) : GameMessage()

    @Serializable
    data class Pass(val playerIndex: Int) : GameMessage()

    @Serializable
    data class FullSync(val state: GameState) : GameMessage()

    @Serializable
    data class PlayerJoined(val player: SessionPlayer) : GameMessage()

    @Serializable
    data class PlayerLeft(val playerIndex: Int) : GameMessage()

    @Serializable
    data class GameConfig(val mode: GameMode, val difficulty: Int) : GameMessage()

    @Serializable
    data object Ping : GameMessage()

    @Serializable
    data object Pong : GameMessage()
}
