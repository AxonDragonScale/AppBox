package com.axondragonscale.logcatviewer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.catching
import com.axondragonscale.commons.getOrDefault
import com.axondragonscale.commons.onFailure
import com.axondragonscale.logcatviewer.model.SavedView
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Created by Ronak on 09/11/26 */

private const val TAG = "LogcatSavedViews"
private const val STORE_NAME = "logcat_viewer"

private val Context.logcatPrefs by preferencesDataStore(STORE_NAME)

private val SAVED_VIEWS = stringPreferencesKey("saved_views")

/** The default persistence: the viewer owns its own DataStore, so a host wires nothing. */
class DataStoreSavedViewsStore(context: Context) : ISavedViewsStore {

    private val dataStore = context.applicationContext.logcatPrefs
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(SavedView.serializer())

    override val views: Flow<List<SavedView>> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs -> decode(prefs[SAVED_VIEWS]) }

    override suspend fun save(view: SavedView) = update { it.saving(view) }

    override suspend fun rename(id: Long, name: String) = update { it.renaming(id, name) }

    override suspend fun delete(id: Long) = update { it.removing(id) }

    private suspend fun update(block: (List<SavedView>) -> List<SavedView>) {
        dataStore.edit { prefs ->
            prefs[SAVED_VIEWS] = json.encodeToString(serializer, block(decode(prefs[SAVED_VIEWS])))
        }
    }

    private fun decode(raw: String?): List<SavedView> {
        if (raw == null) return emptyList()
        return TAG.catching { json.decodeFromString(serializer, raw) }
            .onFailure { if (it !is SerializationException) throw it }
            .getOrDefault(emptyList())
    }
}
