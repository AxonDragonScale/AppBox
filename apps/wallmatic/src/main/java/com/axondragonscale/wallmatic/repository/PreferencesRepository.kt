package com.axondragonscale.wallmatic.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.createPreferencesDataStore
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.pref

/**
 * Created by Ronak Harkhani on 19/06/24
 */

private const val APP_PREFS_STORE = "wallmatic_app_prefs"

class PreferencesRepository {

    private val dataStore = createPreferencesDataStore(APP_PREFS_STORE)

    val uiMode = dataStore.enumPref(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.AUTO,
    )

    val dynamicTheme = dataStore.pref(
        key = booleanPreferencesKey("dynamicTheme"),
        default = false,
    )

    val gridSize = dataStore.pref(
        key = intPreferencesKey("gridSize"),
        default = 2,
    )
}
