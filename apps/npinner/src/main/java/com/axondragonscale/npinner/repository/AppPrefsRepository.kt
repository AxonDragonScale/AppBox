package com.axondragonscale.npinner.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.UIMode
import kotlinx.coroutines.flow.map

/**
 * Created by Ronak Harkhani on 14/05/23
 */

private const val APP_PREFS_STORE = "app_prefs.db"
private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore(APP_PREFS_STORE)

class AppPrefsRepository(
    private val context: Context,
) {

    private val uiModeKey = intPreferencesKey("uiMode")
    val uiModeFlow = context.appPrefs.data.map { prefs ->
        val ordinal = prefs[uiModeKey] ?: UIMode.DEFAULT.ordinal
        UIMode.fromOrdinal(ordinal)
    }

    suspend fun setUiMode(uiMode: UIMode) {
        context.appPrefs.edit { prefs ->
            prefs[uiModeKey] = uiMode.ordinal
        }
    }

}
