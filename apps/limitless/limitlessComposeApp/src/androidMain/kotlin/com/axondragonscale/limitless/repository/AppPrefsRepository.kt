package com.axondragonscale.limitless.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.pref
import com.axondragonscale.limitless.model.UIAlign

/**
 * Created by Ronak Harkhani on 01/11/24
 */

private const val APP_PREFS_STORE = "app_prefs_store"
private val Context.appPrefs by preferencesDataStore(APP_PREFS_STORE)

class AppPrefsRepository(
    private val context: Context,
) {

    val uiMode = context.appPrefs.enumPref(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )

    val dynamicTheme = context.appPrefs.pref(
        key = booleanPreferencesKey("dynamicTheme"),
        default = false,
    )

    val uiAlign = context.appPrefs.enumPref(
        key = stringPreferencesKey("uiAlign"),
        default = UIAlign.DEFAULT,
    )

    val highlightCurrentDay = context.appPrefs.pref(
        key = booleanPreferencesKey("highlightCurrentDay"),
        default = true,
    )

}
