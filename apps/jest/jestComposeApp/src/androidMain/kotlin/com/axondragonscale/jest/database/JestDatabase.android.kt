package com.axondragonscale.jest.database

import android.content.Context
import androidx.room.Room
import org.koin.mp.KoinPlatform

actual fun createJestDatabase(): JestDatabase {
    val context = KoinPlatform.getKoin().get<Context>()
    return Room.databaseBuilder<JestDatabase>(
        context = context,
        name = context.getDatabasePath(JestDatabase.DB_NAME).absolutePath,
    ).build()
}
