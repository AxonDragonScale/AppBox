package com.axondragonscale.jest.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun createJestDatabase(): JestDatabase {
    val dbDir = File(System.getProperty("user.home"), ".local/share/jest")
    if (!dbDir.exists()) dbDir.mkdirs()
    val dbFile = File(dbDir, JestDatabase.DB_NAME)
    return Room.databaseBuilder<JestDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .build()
}
