package com.emmagame.polyomino.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.emmagame.polyomino.model.GameState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_data")

class GameRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    companion object {
        private val SAVED_GAME_KEY = stringPreferencesKey("saved_game")
        private val SAVE_DATE_KEY = stringPreferencesKey("save_date")
    }

    suspend fun saveGame(state: GameState) {
        val serialized = json.encodeToString(state)
        val date = java.time.Instant.now().toString()
        context.gameDataStore.edit { prefs ->
            prefs[SAVED_GAME_KEY] = serialized
            prefs[SAVE_DATE_KEY] = date
        }
    }

    suspend fun loadGame(): GameState? {
        return try {
            val prefs = context.gameDataStore.data.first()
            val serialized = prefs[SAVED_GAME_KEY] ?: return null
            json.decodeFromString<GameState>(serialized)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getSaveDate(): String? {
        return try {
            val prefs = context.gameDataStore.data.first()
            prefs[SAVE_DATE_KEY]
        } catch (_: Exception) {
            null
        }
    }

    suspend fun hasSavedGame(): Boolean {
        return try {
            val prefs = context.gameDataStore.data.first()
            prefs[SAVED_GAME_KEY] != null
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteSavedGame() {
        context.gameDataStore.edit { prefs ->
            prefs.remove(SAVED_GAME_KEY)
            prefs.remove(SAVE_DATE_KEY)
        }
    }
}
