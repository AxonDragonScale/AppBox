package com.axondragonscale.appbox.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.appbox.model.AppThemeMode
import com.axondragonscale.commons.enumSetting
import com.axondragonscale.commons.setting

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppSettings(context: Context) {

    private val dataStore = context.dataStore

    val themeMode = dataStore.enumSetting(
        key = stringPreferencesKey("theme_mode"),
        default = AppThemeMode.SYSTEM,
    )

    val isDynamicTheme = dataStore.setting(
        key = booleanPreferencesKey("dynamic_theme"),
        default = true,
    )

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
