package com.axondragonscale.commons.room

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Created by Ronak Harkhani on 08 March 2026.
 */

actual inline fun <reified T : RoomDatabase> createDatabase(name: String): T {
    val docDir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbPath = requireNotNull(docDir?.path) { "Could not resolve NSDocumentDirectory" }
    return Room.databaseBuilder<T>(name = "$dbPath/$name",)
        .setDriver(BundledSQLiteDriver())
        .build()
}
