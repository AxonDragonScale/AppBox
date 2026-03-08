package com.axondragonscale.jest.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.createPreferencesDataStore
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.pref
import com.axondragonscale.commons.serializedPref
import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.JokeType

class PrefsRepository {

    private val dataStore = createPreferencesDataStore("jest_prefs")

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
