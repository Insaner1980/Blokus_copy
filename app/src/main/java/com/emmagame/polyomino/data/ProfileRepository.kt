package com.emmagame.polyomino.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profiles")

class ProfileRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val profilesKey = stringPreferencesKey("profiles")
    private val settingsKey = stringPreferencesKey("settings")

    val profiles: Flow<List<Profile>> = context.profileDataStore.data.map { prefs ->
        val serialized = prefs[profilesKey] ?: return@map emptyList()
        try {
            json.decodeFromString<List<Profile>>(serialized)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getProfiles(): List<Profile> = profiles.first()

    suspend fun getActiveProfile(): Profile? =
        getProfiles().firstOrNull { it.active }

    suspend fun saveProfiles(profiles: List<Profile>) {
        val serialized = json.encodeToString(profiles)
        context.profileDataStore.edit { prefs ->
            prefs[profilesKey] = serialized
        }
    }

    suspend fun addProfile(profile: Profile) {
        val existing = getProfiles().toMutableList()
        val newProfile = if (existing.isEmpty()) profile.copy(active = true) else profile
        existing.add(newProfile)
        saveProfiles(existing)
    }

    suspend fun updateProfile(profile: Profile) {
        val existing = getProfiles().toMutableList()
        val idx = existing.indexOfFirst { it.id == profile.id }
        if (idx >= 0) {
            existing[idx] = profile
            saveProfiles(existing)
        }
    }

    suspend fun deleteProfile(profileId: Long) {
        val existing = getProfiles().toMutableList()
        val wasActive = existing.firstOrNull { it.id == profileId }?.active == true
        existing.removeAll { it.id == profileId }
        if (wasActive && existing.isNotEmpty()) {
            existing[0] = existing[0].copy(active = true)
        }
        saveProfiles(existing)
    }

    suspend fun setActiveProfile(profileId: Long) {
        val existing = getProfiles().map { it.copy(active = it.id == profileId) }
        saveProfiles(existing)
    }

    suspend fun recordGameHistory(profileId: Long, entry: HistoryEntry) {
        val existing = getProfiles().toMutableList()
        val idx = existing.indexOfFirst { it.id == profileId }
        if (idx >= 0) {
            existing[idx] = existing[idx].withHistoryEntry(entry)
            saveProfiles(existing)
        }
    }

    // --- Settings ---

    val settings: Flow<AppSettings> = context.profileDataStore.data.map { prefs ->
        val serialized = prefs[settingsKey] ?: return@map AppSettings()
        try {
            json.decodeFromString<AppSettings>(serialized)
        } catch (_: Exception) {
            AppSettings()
        }
    }

    suspend fun getSettings(): AppSettings = settings.first()

    suspend fun saveSettings(settings: AppSettings) {
        val serialized = json.encodeToString(settings)
        context.profileDataStore.edit { prefs ->
            prefs[settingsKey] = serialized
        }
    }

    companion object {
        fun calculateStats(history: List<HistoryEntry>): GameStats {
            if (history.isEmpty()) return GameStats()

            val totalGames = history.size
            val wins = history.count { it.rank == 1 }
            val winRate = if (totalGames > 0) wins.toFloat() / totalGames else 0f
            val avgScore = history.map { it.score }.average().toFloat()
            val bestScore = history.minOf { it.score }
            val avgRank = history.map { it.rank }.average().toFloat()

            val diffCounts = history.groupBy { it.difficulty }
            val favoriteDifficulty = diffCounts.maxByOrNull { it.value.size }?.key ?: 3

            val recentScores = history.take(20).map { it.score }

            val statsByDifficulty = diffCounts.mapValues { (_, entries) ->
                DifficultyStats(
                    games = entries.size,
                    wins = entries.count { it.rank == 1 },
                    avgScore = entries.map { it.score }.average().toFloat()
                )
            }

            return GameStats(
                totalGames = totalGames,
                wins = wins,
                winRate = winRate,
                averageScore = avgScore,
                bestScore = bestScore,
                averageRank = avgRank,
                favoriteDifficulty = favoriteDifficulty,
                recentScores = recentScores,
                statsByDifficulty = statsByDifficulty
            )
        }
    }
}
