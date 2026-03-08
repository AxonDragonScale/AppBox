package com.axondragonscale.limitless.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.createPreferencesDataStore
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.pref
import com.axondragonscale.limitless.model.UIAlign

/**
 * Created by Ronak Harkhani on 01/11/24
 */

private const val APP_PREFS_STORE = "app_prefs_store"

class AppPrefsRepository {

    private val dataStore = createPreferencesDataStore(APP_PREFS_STORE)

    val uiMode = dataStore.enumPref(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )

    val dynamicTheme = dataStore.pref(
        key = booleanPreferencesKey("dynamicTheme"),
        default = false,
    )

    val uiAlign = dataStore.enumPref(
        key = stringPreferencesKey("uiAlign"),
        default = UIAlign.DEFAULT,
    )

    val highlightCurrentDay = dataStore.pref(
        key = booleanPreferencesKey("highlightCurrentDay"),
        default = true,
    )

}
