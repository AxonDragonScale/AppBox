package com.axondragonscale.jest.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.serializedPref
import com.axondragonscale.commons.pref
import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.JokeType

private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore("jest_prefs")

class PrefsRepository(context: Context) {

    private val dataStore = context.appPrefs

    val jokeCategories = dataStore.serializedPref(
        key = "jokeCategories",
        default = emptyList<Category>(),
    )

    val jokeTypes = dataStore.serializedPref(
        key = "jokeTypes",
        default = emptyList<JokeType>(),
    )

    val blacklistFlags = dataStore.serializedPref(
        key = "blacklistFlags",
        default = emptyList<BlacklistFlag>(),
    )

    val uiMode = dataStore.enumPref(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )

    val dynamicTheme = dataStore.pref(
        key = booleanPreferencesKey("dynamicTheme"),
        default = true,
    )
}
