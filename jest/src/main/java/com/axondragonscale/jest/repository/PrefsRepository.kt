package com.axondragonscale.jest.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.jest.model.BlacklistFlag
import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.JokeType
import kotlinx.coroutines.flow.map

/**
 * Created by Ronak Harkhani on 06/04/24
 */

private const val APP_PREFS_STORE = "app_prefs"
private val Context.appPrefs: DataStore<Preferences> by preferencesDataStore(APP_PREFS_STORE)

class PrefsRepository(
    private val context: Context,
) {

    private val jokeCategoriesKey = stringSetPreferencesKey("jokeCategories")
    val jokeCategoriesFlow = context.appPrefs.data.map { prefs ->
        val categories = prefs[jokeCategoriesKey] ?: setOf()
        categories.map { Category.fromOrdinal(it.toInt()) }
    }

    suspend fun setJokeCategories(categories: List<Category>) {
        context.appPrefs.edit { prefs ->
            prefs[jokeCategoriesKey] = categories.map { it.ordinal.toString() }.toSet()
        }
    }

    private val jokeTypesKey = stringSetPreferencesKey("jokeTypes")
    val jokeTypesFlow = context.appPrefs.data.map { prefs ->
        val types = prefs[jokeTypesKey] ?: setOf()
        types.map { JokeType.fromOrdinal(it.toInt()) }
    }

    suspend fun setJokeTypes(jokeTypes: List<JokeType>) {
        context.appPrefs.edit { prefs ->
            prefs[jokeTypesKey] = jokeTypes.map { it.ordinal.toString() }.toSet()
        }
    }

    private val blacklistFlagsKey = stringSetPreferencesKey("blacklistFlags")
    val blacklistFlagsFlow = context.appPrefs.data.map { prefs ->
        val blacklistFlags = prefs[blacklistFlagsKey] ?: setOf()
        blacklistFlags.map { BlacklistFlag.fromOrdinal(it.toInt()) }
    }

    suspend fun setBlacklistFlags(blacklistFlags: List<BlacklistFlag>) {
        context.appPrefs.edit { prefs ->
            prefs[blacklistFlagsKey] = blacklistFlags.map { it.ordinal.toString() }.toSet()
        }
    }
}
