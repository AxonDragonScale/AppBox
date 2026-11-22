package com.axondragonscale.commons.devtools.logs.data

import android.content.Context
import androidx.room.Room

/**
 * Opens the persisted log database under the app's data directory and starts copying every
 * `Logger` record into it. Call once, early, and only from a build type that keeps dev tools.
 */
internal fun installDevLogStore(context: Context) {
    val appContext = context.applicationContext
    DevLogStore.install {
        Room.databaseBuilder(appContext, DevLogDatabase::class.java, DEV_LOG_DB_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}
