package com.axondragonscale.qrqanvas.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.enumSetting
import com.axondragonscale.commons.setting

/**
 * Created by Ronak Harkhani on 09/07/25
 */

private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore("qrqanvas_prefs")

class AppPrefsRepository(context: Context) {

    private val dataStore = context.appPrefs

    val uiMode = dataStore.enumSetting(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )

    val dynamicTheme = dataStore.setting(
        key = booleanPreferencesKey("dynamicTheme"),
        default = true,
    )
}
