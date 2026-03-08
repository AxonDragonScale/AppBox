package com.axondragonscale.jest.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.axondragonscale.jest.database.converter.CategoryConverter
import com.axondragonscale.jest.database.converter.JokeTypeConverter
import com.axondragonscale.jest.database.converter.LanguageConverter
import com.axondragonscale.jest.database.dao.JokeDao
import com.axondragonscale.jest.database.entity.JokeEntity

/**
 * Created by Ronak Harkhani on 03/04/24
 */
@Database(
    entities = [
        JokeEntity::class,
    ],
    views = [],
    version = JestDatabase.DB_VERSION,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(
    CategoryConverter::class,
    JokeTypeConverter::class,
    LanguageConverter::class,
)
@ConstructedBy(JestDatabaseConstructor::class)
abstract class JestDatabase : RoomDatabase() {

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "JestDatabase.db"
    }

    abstract fun getJokeDao(): JokeDao

}

// The KSP-generated implementation of this is suppressed because Room generates it.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object JestDatabaseConstructor : RoomDatabaseConstructor<JestDatabase> {
    override fun initialize(): JestDatabase
}

/**
 * Platform-specific factory function to create the [JestDatabase].
 * Each platform provides its own Room builder configuration.
 */
expect fun createJestDatabase(): JestDatabase
