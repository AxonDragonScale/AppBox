package com.axondragonscale.topty.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.enumSetting
import com.axondragonscale.commons.setting
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.SortOrder
import com.axondragonscale.topty.model.UIFont
import com.axondragonscale.commons.UIMode

private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore("topty_prefs")

class ToptyPrefsRepository(context: Context) {

    private val dataStore = context.appPrefs

    val uiMode = dataStore.enumSetting(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )

    val dynamicTheme = dataStore.setting(
        key = booleanPreferencesKey("dynamicTheme"),
        default = false,
    )

    val codeDesign = dataStore.enumSetting(
        key = stringPreferencesKey("codeDesign"),
        default = CodeDesign.DEFAULT,
    )

    val uiFont = dataStore.enumSetting(
        key = stringPreferencesKey("uiFont"),
        default = UIFont.DEFAULT,
    )

    val speakOutOnTap = dataStore.setting(
        key = booleanPreferencesKey("speakOutOnTap"),
        default = false,
    )

    val biometricLock = dataStore.setting(
        key = booleanPreferencesKey("biometricLock"),
        default = false,
    )

    val preventScreenshots = dataStore.setting(
        key = booleanPreferencesKey("preventScreenshots"),
        default = true,
    )

    val sortOrder = dataStore.enumSetting(
        key = stringPreferencesKey("sortOrder"),
        default = SortOrder.DEFAULT,
    )

    val hasCompletedOnboarding = dataStore.setting(
        key = booleanPreferencesKey("hasCompletedOnboarding"),
        default = false,
    )

    val passwordHash = dataStore.setting(
        key = stringPreferencesKey("passwordHash"),
        default = "",
    )

    val passwordHashSalt = dataStore.setting(
        key = stringPreferencesKey("passwordHashSalt"),
        default = "",
    )

    val keyDerivationSalt = dataStore.setting(
        key = stringPreferencesKey("keyDerivationSalt"),
        default = "",
    )

}
