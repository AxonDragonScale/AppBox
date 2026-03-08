package com.axondragonscale.jest.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun createJestDatabase(): JestDatabase {
    val docDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbPath = requireNotNull(docDir?.path) { "Could not resolve NSDocumentDirectory" }
    return Room.databaseBuilder<JestDatabase>(
        name = "$dbPath/${JestDatabase.DB_NAME}",
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}
