package com.axondragonscale.npinner.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.enumSetting

/**
 * Created by Ronak Harkhani on 14/05/23
 */

private const val APP_PREFS_STORE = "app_prefs.db"
private val Context.appPrefs by preferencesDataStore(APP_PREFS_STORE)

class AppPrefsRepository(
    private val context: Context,
) {

    val uiMode = context.appPrefs.enumSetting(
        key = stringPreferencesKey("uiMode"),
        default = UIMode.DEFAULT,
    )

}
