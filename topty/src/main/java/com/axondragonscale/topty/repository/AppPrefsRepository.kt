package com.axondragonscale.topty.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.UIFont
import com.axondragonscale.topty.model.UIMode
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore("topty_prefs")

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

    // MARK: Code Design ---------------------------------------------------------------------------
    private val codeDesignKey = intPreferencesKey("codeDesign")
    val codeDesignFlow = context.appPrefs.data
        .map { prefs -> CodeDesign.fromOrdinal(prefs[codeDesignKey] ?: CodeDesign.DEFAULT.ordinal) }
        .distinctUntilChanged()

    suspend fun setCodeDesign(codeDesign: CodeDesign) {
        context.appPrefs.edit { prefs ->
            prefs[codeDesignKey] = codeDesign.ordinal
        }
    }

    // MARK: UI Font -------------------------------------------------------------------------------
    private val uiFontKey = intPreferencesKey("uiFont")
    val uiFontFlow = context.appPrefs.data
        .map { prefs -> UIFont.fromOrdinal(prefs[uiFontKey] ?: UIFont.DEFAULT.ordinal) }
        .distinctUntilChanged()

    suspend fun setUIFont(uiFont: UIFont) {
        context.appPrefs.edit { prefs ->
            prefs[uiFontKey] = uiFont.ordinal
        }
    }

    // MARK: Speak Out on Tap ----------------------------------------------------------------------
    private val speakOutOnTapKey = booleanPreferencesKey("speakOutOnTap")
    val speakOutOnTapFlow = context.appPrefs.data
        .map { prefs -> prefs[speakOutOnTapKey] ?: false }
        .distinctUntilChanged()

    suspend fun setSpeakOutOnTap(speakOutOnTap: Boolean) {
        context.appPrefs.edit { prefs ->
            prefs[speakOutOnTapKey] = speakOutOnTap
        }
    }

}
