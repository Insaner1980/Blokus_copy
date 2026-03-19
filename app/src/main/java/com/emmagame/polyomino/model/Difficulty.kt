package com.emmagame.polyomino.model

import kotlinx.serialization.Serializable

@Serializable
enum class Difficulty(
    val level: Int,
    val displayName: String,
    val temperature: Float,
    val skipFilterChance: Float,
    val localFocusRadius: Int,
    val maxCandidates: Int
) {
    BEGINNER(1, "Beginner", 3.0f, 0.7f, 3, 10),
    EASY(2, "Easy", 2.0f, 0.4f, 5, 25),
    MEDIUM(3, "Medium", 1.0f, 0.1f, 20, 80),
    HARD(4, "Hard", 0.5f, 0.0f, 20, 200),
    EXPERT(5, "Expert", 0.2f, 0.0f, 20, 500);
}
