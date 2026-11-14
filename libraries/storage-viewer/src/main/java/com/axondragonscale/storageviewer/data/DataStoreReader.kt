package com.axondragonscale.storageviewer.data

import androidx.datastore.preferences.core.PreferencesFileSerializer
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.model.PreferenceType
import java.io.FileInputStream

/** Created by Ronak on 14/11/26 */

/**
 * Reads one Preferences DataStore file in place. A plain [FileInputStream] is used, so nothing is
 * created, locked or flushed; a corrupt proto raises and the caller reports it as unreadable.
 */
internal object DataStoreReader {

    suspend fun entries(path: String): List<PreferenceEntry> {
        val preferences = FileInputStream(path).use { input ->
            PreferencesFileSerializer.readFrom(input)
        }
        return preferences.asMap()
            .map { (key, value) -> entry(key.name, value) }
            .sortedBy { entry -> entry.key }
    }
}

private fun entry(key: String, value: Any): PreferenceEntry = when (value) {
    is Boolean -> entry(key, PreferenceType.BOOL, value.toString())
    is Int -> entry(key, PreferenceType.INT, value.toString())
    is Long -> entry(key, PreferenceType.LONG, value.toString())
    is Float -> entry(key, PreferenceType.FLOAT, value.toString())
    is Double -> entry(key, PreferenceType.DOUBLE, value.toString())
    is String -> entry(key, PreferenceType.STRING, value)
    is Set<*> -> entry(key, PreferenceType.STRING_SET, setDisplay(value))
    else -> entry(key, PreferenceType.BYTES, value.toString())
}

private fun entry(key: String, type: PreferenceType, value: String): PreferenceEntry {
    val display = displayedText(value)
    return PreferenceEntry(key, type, display.text, display.byteCount, display.truncated)
}
