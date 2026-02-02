package com.axondragonscale.appbox.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

class AppSettings(private val context: Context) {

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE]?.let { AppThemeMode.valueOf(it) } ?: AppThemeMode.SYSTEM
    }

    val isDynamicTheme: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_THEME] ?: true }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { settings ->
            settings[THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicTheme(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[DYNAMIC_THEME] = enabled
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
