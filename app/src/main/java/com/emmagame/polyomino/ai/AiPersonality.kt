package com.emmagame.polyomino.ai

import kotlinx.serialization.Serializable

@Serializable
enum class AiPersonality(
    val displayName: String,
    val spreadBonus: Float,
    val cornerBonus: Float,
    val centerBonus: Float,
    val blockingBonus: Float,
    val edgeBonus: Float
) {
    EXPANSIONIST(
        displayName = "Expansionist",
        spreadBonus = 1.4f,
        cornerBonus = 1.4f,
        centerBonus = 0.3f,
        blockingBonus = 0.6f,
        edgeBonus = 0.4f
    ),
    AGGRESSIVE(
        displayName = "Aggressive",
        spreadBonus = 1.0f,
        cornerBonus = 1.0f,
        centerBonus = 1.5f,
        blockingBonus = 1.6f,
        edgeBonus = 0.3f
    ),
    DEFENSIVE(
        displayName = "Defensive",
        spreadBonus = 0.6f,
        cornerBonus = 1.0f,
        centerBonus = 0.1f,
        blockingBonus = 0.8f,
        edgeBonus = 1.2f
    );

    companion object {
        fun forPlayerIndex(index: Int): AiPersonality = when (index % 3) {
            0 -> EXPANSIONIST
            1 -> AGGRESSIVE
            else -> DEFENSIVE
        }
    }
}
