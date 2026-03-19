package com.emmagame.polyomino.data

import com.emmagame.polyomino.model.GameConstants
import com.emmagame.polyomino.model.GameMode
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: Long,
    val name: String,
    val colorIndex: Int = 0,
    val avatarStyle: String = "bottts",
    val avatarSeed: String = "",
    val active: Boolean = false,
    val history: List<HistoryEntry> = emptyList()
) {
    fun withHistoryEntry(entry: HistoryEntry): Profile {
        val newHistory = (listOf(entry) + history).take(GameConstants.MAX_HISTORY_ENTRIES)
        return copy(history = newHistory)
    }
}

@Serializable
data class HistoryEntry(
    val date: String,
    val rank: Int,
    val score: Int,
    val difficulty: Int,
    val gameMode: GameMode,
    val time: Int,
    val scores: List<PlayerScore>
)

@Serializable
data class PlayerScore(
    val name: String,
    val score: Int,
    val colorIndex: Int
)

@Serializable
data class AppSettings(
    val lastDifficulty: Int = 3,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

data class GameStats(
    val totalGames: Int = 0,
    val wins: Int = 0,
    val winRate: Float = 0f,
    val averageScore: Float = 0f,
    val bestScore: Int = GameConstants.TOTAL_CELLS,
    val averageRank: Float = 0f,
    val favoriteDifficulty: Int = 3,
    val recentScores: List<Int> = emptyList(),
    val statsByDifficulty: Map<Int, DifficultyStats> = emptyMap()
)

data class DifficultyStats(
    val games: Int = 0,
    val wins: Int = 0,
    val avgScore: Float = 0f
)
