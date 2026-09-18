package com.tennisscore.watch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tennisscore.watch.model.AppState
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tennis_watch_state")

private val APP_STATE_KEY = stringPreferencesKey("app_state_json")

interface MatchRepository {
    suspend fun load(): AppState
    suspend fun save(state: AppState)
}

class DataStoreMatchRepository(private val context: Context) : MatchRepository {

    override suspend fun load(): AppState {
        return try {
            val prefs = context.dataStore.data.first()
            val raw = prefs[APP_STATE_KEY] ?: return AppState(language = systemDefaultLanguage())
            AppStateJson.decode(raw)
        } catch (e: Exception) {
            AppState(language = systemDefaultLanguage())
        }
    }

    override suspend fun save(state: AppState) {
        val raw = AppStateJson.encode(state)
        context.dataStore.edit { it[APP_STATE_KEY] = raw }
    }
}
