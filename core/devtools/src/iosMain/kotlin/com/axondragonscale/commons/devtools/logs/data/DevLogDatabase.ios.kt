package com.axondragonscale.commons.devtools.logs.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Opens the persisted log database in the documents directory and starts copying every `Logger`
 * record into it. Call once, early, and only from a dev build.
 */
internal fun installDevLogStore() {
    DevLogStore.install {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        val path = requireNotNull(directory?.path) { "Could not resolve NSDocumentDirectory" }
        Room.databaseBuilder<DevLogDatabase>(name = "$path/$DEV_LOG_DB_NAME")
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
