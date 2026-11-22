package com.axondragonscale.commons.devtools.logs.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

/**
 * Opens the persisted log database under the user's home and starts copying every `Logger` record
 * into it. Call once, early, and only from a dev build.
 */
internal fun installDevLogStore() {
    DevLogStore.install {
        val directory = File(System.getProperty("user.home"), ".appbox").apply { mkdirs() }
        Room.databaseBuilder<DevLogDatabase>(name = File(directory, DEV_LOG_DB_NAME).absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
