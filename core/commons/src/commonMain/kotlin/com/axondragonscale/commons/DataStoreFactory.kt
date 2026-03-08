package com.axondragonscale.commons

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Created by Ronak Harkhani on 07 March 2026.
 *
 * Platform-specific path where the DataStore preferences file should be stored.
 */
expect fun dataStorePreferencesPath(name: String): String

/**
 * Creates a [DataStore] backed by [Preferences] for the given file name.
 * Uses the platform-specific path from [dataStorePreferencesPath].
 */
fun createPreferencesDataStore(name: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { dataStorePreferencesPath(name).toPath() }
    )
