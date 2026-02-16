package com.axondragonscale.limitless.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.limitless.model.UIAlign
import com.axondragonscale.limitless.model.UIMode
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Created by Ronak Harkhani on 01/11/24
 */

private const val APP_PREFS_STORE = "app_prefs_store"
private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore(APP_PREFS_STORE)

class AppPrefsRepository(
    private val context: Context,
) {

    // MARK: UI Mode -------------------------------------------------------------------------------
    private val uiModeKey = intPreferencesKey("uiMode")
    val uiModeFlow = context.appPrefs.data
        .map { prefs -> UIMode.fromOrdinal(prefs[uiModeKey] ?: UIMode.DEFAULT.ordinal) }
        .distinctUntilChanged()

    suspend fun setUiMode(uiMode: UIMode) {
        context.appPrefs.edit { prefs ->
            prefs[uiModeKey] = uiMode.ordinal
        }
    }

    // MARK: Dynamic Theme -------------------------------------------------------------------------
    private val dynamicThemeKey = booleanPreferencesKey("dynamicTheme")
    val dynamicThemeFlow = context.appPrefs.data
        .map { prefs -> prefs[dynamicThemeKey] ?: false }
        .distinctUntilChanged()

    suspend fun setDynamicTheme(dynamicTheme: Boolean) {
        context.appPrefs.edit { prefs ->
            prefs[dynamicThemeKey] = dynamicTheme
        }
    }

    // MARK: UI Align ------------------------------------------------------------------------------
    private val uiAlignKey = intPreferencesKey("uiAlign")
    val uiAlignFlow = context.appPrefs.data
        .map { prefs -> UIAlign.fromOrdinal(prefs[uiAlignKey] ?: UIAlign.DEFAULT.ordinal) }
        .distinctUntilChanged()

    suspend fun setUiAlign(uiAlign: UIAlign) {
        context.appPrefs.edit { prefs ->
            prefs[uiAlignKey] = uiAlign.ordinal
        }
    }

}
