package com.axondragonscale.jest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axondragonscale.jest.database.JestDatabase.Companion.DB_VERSION
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
    version = DB_VERSION,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(
    CategoryConverter::class,
    JokeTypeConverter::class,
    LanguageConverter::class,
)
internal abstract class JestDatabase : RoomDatabase() {

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "JestDatabase"

        fun create(appContext: Context): JestDatabase =
            Room.databaseBuilder(
                context = appContext,
                klass = JestDatabase::class.java,
                name = DB_NAME
            ).build()

    }

    abstract fun getJokeDao(): JokeDao

}
