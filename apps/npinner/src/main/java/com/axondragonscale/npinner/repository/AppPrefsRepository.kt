package com.axondragonscale.npinner.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.npinner.model.DarkModeConfig
import kotlinx.coroutines.flow.map

/**
 * Created by Ronak Harkhani on 14/05/23
 */

private const val APP_PREFS_STORE = "app_prefs.db"
private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore(APP_PREFS_STORE)

class AppPrefsRepository(
    private val context: Context,
) {

    private val darkModeConfigKey = intPreferencesKey("darkModeConfig")
    val darkModeConfigFlow = context.appPrefs.data.map { prefs ->
        val ordinal = prefs[darkModeConfigKey] ?: DarkModeConfig.AUTO.ordinal
        DarkModeConfig.fromOrdinal(ordinal)
    }

    suspend fun setDarkModeConfig(darkModeConfig: DarkModeConfig) {
        context.appPrefs.edit { prefs ->
            prefs[darkModeConfigKey] = darkModeConfig.ordinal
        }
    }

}
