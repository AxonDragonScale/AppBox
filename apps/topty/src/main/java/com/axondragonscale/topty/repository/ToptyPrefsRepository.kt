package com.axondragonscale.topty.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.pref
import com.axondragonscale.topty.model.CodeDesign
import com.axondragonscale.topty.model.SortOrder
import com.axondragonscale.topty.model.UIFont

private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore("topty_prefs")

class ToptyPrefsRepository(context: Context) {

    private val dataStore = context.appPrefs

    val uiMode = dataStore.enumPref(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )

    val dynamicTheme = dataStore.pref(
        key = booleanPreferencesKey("dynamicTheme"),
        default = false,
    )

    val codeDesign = dataStore.enumPref(
        key = stringPreferencesKey("codeDesign"),
        default = CodeDesign.DEFAULT,
    )

    val uiFont = dataStore.enumPref(
        key = stringPreferencesKey("uiFont"),
        default = UIFont.DEFAULT,
    )

    val speakOutOnTap = dataStore.pref(
        key = booleanPreferencesKey("speakOutOnTap"),
        default = false,
    )

    val biometricLock = dataStore.pref(
        key = booleanPreferencesKey("biometricLock"),
        default = false,
    )

    val preventScreenshots = dataStore.pref(
        key = booleanPreferencesKey("preventScreenshots"),
        default = true,
    )

    val sortOrder = dataStore.enumPref(
        key = stringPreferencesKey("sortOrder"),
        default = SortOrder.DEFAULT,
    )

    val hasCompletedOnboarding = dataStore.pref(
        key = booleanPreferencesKey("hasCompletedOnboarding"),
        default = false,
    )

    val passwordHash = dataStore.pref(
        key = stringPreferencesKey("passwordHash"),
        default = "",
    )

    val passwordHashSalt = dataStore.pref(
        key = stringPreferencesKey("passwordHashSalt"),
        default = "",
    )

    val keyDerivationSalt = dataStore.pref(
        key = stringPreferencesKey("keyDerivationSalt"),
        default = "",
    )

}
