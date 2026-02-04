package com.axondragonscale.jest.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.serializedSetting
import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.JokeType

private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore("jest_prefs")

class PrefsRepository(context: Context) {

    private val dataStore = context.appPrefs

    val jokeCategories = dataStore.serializedSetting(
        key = "jokeCategories",
        default = emptyList<Category>(),
    )

    val jokeTypes = dataStore.serializedSetting(
        key = "jokeTypes",
        default = emptyList<JokeType>(),
    )

    val blacklistFlags = dataStore.serializedSetting(
        key = "blacklistFlags",
        default = emptyList<BlacklistFlag>(),
    )
}
