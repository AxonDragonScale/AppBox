package com.axondragonscale.commons.room

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

/**
 * Created by Ronak Harkhani on 08 March 2026.
 */

actual inline fun <reified T : RoomDatabase> createDatabase(name: String): T {
    val dbDir = File(System.getProperty("user.home"), ".local/share/jest")
    if (!dbDir.exists()) dbDir.mkdirs()
    val dbFile = File(dbDir, name)
    return Room.databaseBuilder<T>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .build()
}
