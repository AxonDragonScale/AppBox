package com.axondragonscale.commons.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.mp.KoinPlatform

/**
 * Created by Ronak Harkhani on 08 March 2026.
 */

actual inline fun <reified T: RoomDatabase> createDatabase(name: String): T {
    val context = KoinPlatform.getKoin().get<Context>()
    return Room.databaseBuilder<T>(
        context = context,
        name = context.getDatabasePath(name).absolutePath,
    ).build()
}