package com.emmagame.polyomino.data

import com.emmagame.polyomino.model.GameConstants
import com.emmagame.polyomino.model.GameMode
import org.junit.Assert.*
import org.junit.Test

class ProfileStatsTest {

    @Test
    fun `new profile has empty history`() {
        val profile = Profile(id = 1L, name = "Test")
        assertTrue(profile.history.isEmpty())
    }

    @Test
    fun `withHistoryEntry adds entry at front`() {
        val profile = Profile(id = 1L, name = "Test")
        val entry = createEntry(rank = 1, score = 50)
        val updated = profile.withHistoryEntry(entry)
        assertEquals(1, updated.history.size)
        assertEquals(50, updated.history[0].score)
    }

    @Test
    fun `withHistoryEntry caps at MAX_HISTORY_ENTRIES`() {
        var profile = Profile(id = 1L, name = "Test")
        for (i in 1..60) {
            profile = profile.withHistoryEntry(createEntry(rank = 1, score = i))
        }
        assertEquals(GameConstants.MAX_HISTORY_ENTRIES, profile.history.size)
        assertEquals(60, profile.history[0].score)
    }

    @Test
    fun `calculateStats with empty history returns defaults`() {
        val stats = ProfileRepository.calculateStats(emptyList())
        assertEquals(0, stats.totalGames)
        assertEquals(0, stats.wins)
        assertEquals(0f, stats.winRate)
    }

    @Test
    fun `calculateStats computes correct totals`() {
        val history = listOf(
            createEntry(rank = 1, score = 30),
            createEntry(rank = 2, score = 50),
            createEntry(rank = 1, score = 20),
            createEntry(rank = 3, score = 60)
        )
        val stats = ProfileRepository.calculateStats(history)
        assertEquals(4, stats.totalGames)
        assertEquals(2, stats.wins)
        assertEquals(0.5f, stats.winRate, 0.001f)
        assertEquals(40f, stats.averageScore, 0.001f)
        assertEquals(20, stats.bestScore)
    }

    @Test
    fun `calculateStats computes per-difficulty stats`() {
        val history = listOf(
            createEntry(rank = 1, score = 30, difficulty = 3),
            createEntry(rank = 2, score = 50, difficulty = 3),
            createEntry(rank = 1, score = 20, difficulty = 5)
        )
        val stats = ProfileRepository.calculateStats(history)
        assertEquals(2, stats.statsByDifficulty.size)
        assertEquals(2, stats.statsByDifficulty[3]?.games)
        assertEquals(1, stats.statsByDifficulty[5]?.games)
        assertEquals(1, stats.statsByDifficulty[3]?.wins)
        assertEquals(1, stats.statsByDifficulty[5]?.wins)
    }

    @Test
    fun `calculateStats finds favorite difficulty`() {
        val history = listOf(
            createEntry(difficulty = 3),
            createEntry(difficulty = 3),
            createEntry(difficulty = 3),
            createEntry(difficulty = 5),
            createEntry(difficulty = 1)
        )
        val stats = ProfileRepository.calculateStats(history)
        assertEquals(3, stats.favoriteDifficulty)
    }

    @Test
    fun `calculateStats recent scores returns last 20`() {
        val history = (1..30).map { createEntry(score = it) }
        val stats = ProfileRepository.calculateStats(history)
        assertEquals(20, stats.recentScores.size)
        assertEquals(1, stats.recentScores[0])
    }

    @Test
    fun `calculateStats average rank`() {
        val history = listOf(
            createEntry(rank = 1),
            createEntry(rank = 2),
            createEntry(rank = 3)
        )
        val stats = ProfileRepository.calculateStats(history)
        assertEquals(2f, stats.averageRank, 0.001f)
    }

    @Test
    fun `profile serialization roundtrip`() {
        val profile = Profile(
            id = 123L,
            name = "Test Player",
            colorIndex = 2,
            avatarStyle = "bottts",
            avatarSeed = "abc",
            active = true,
            history = listOf(createEntry(rank = 1, score = 42))
        )
        val json = kotlinx.serialization.json.Json { prettyPrint = false }
        val serialized = json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(Profile.serializer()),
            listOf(profile)
        )
        val deserialized = json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(Profile.serializer()),
            serialized
        )
        assertEquals(1, deserialized.size)
        assertEquals("Test Player", deserialized[0].name)
        assertEquals(2, deserialized[0].colorIndex)
        assertTrue(deserialized[0].active)
        assertEquals(1, deserialized[0].history.size)
        assertEquals(42, deserialized[0].history[0].score)
    }

    @Test
    fun `HistoryEntry serialization roundtrip`() {
        val entry = createEntry(rank = 2, score = 55, difficulty = 4)
        val json = kotlinx.serialization.json.Json { prettyPrint = false }
        val serialized = json.encodeToString(HistoryEntry.serializer(), entry)
        val deserialized = json.decodeFromString(HistoryEntry.serializer(), serialized)
        assertEquals(2, deserialized.rank)
        assertEquals(55, deserialized.score)
        assertEquals(4, deserialized.difficulty)
        assertEquals(GameMode.SOLO, deserialized.gameMode)
    }

    // --- Helpers ---

    private fun createEntry(
        rank: Int = 1,
        score: Int = 40,
        difficulty: Int = 3,
        gameMode: GameMode = GameMode.SOLO,
        time: Int = 300
    ) = HistoryEntry(
        date = "2026-03-19T12:00:00Z",
        rank = rank,
        score = score,
        difficulty = difficulty,
        gameMode = gameMode,
        time = time,
        scores = listOf(
            PlayerScore("Player", score, 0),
            PlayerScore("AI 1", 60, 1),
            PlayerScore("AI 2", 70, 2),
            PlayerScore("AI 3", 80, 3)
        )
    )
}
