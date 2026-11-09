package com.axondragonscale.commons

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object DataStorePathProvider : KoinComponent {
    val context: Context by inject()
}

actual fun dataStorePreferencesPath(name: String): String =
    "${DataStorePathProvider.context.filesDir.absolutePath}/datastore/$name.preferences_pb"
