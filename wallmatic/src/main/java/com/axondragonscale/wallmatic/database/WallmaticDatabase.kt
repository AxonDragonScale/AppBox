package com.axondragonscale.wallmatic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axondragonscale.wallmatic.database.WallmaticDatabase.Companion.DB_VERSION
import com.axondragonscale.wallmatic.database.converter.IntListConverter
import com.axondragonscale.wallmatic.database.converter.StringListConverter
import com.axondragonscale.wallmatic.database.dao.WallmaticDao
import com.axondragonscale.wallmatic.database.entity.Album
import com.axondragonscale.wallmatic.database.entity.Folder
import com.axondragonscale.wallmatic.database.entity.Wallpaper

/**
 * Created by Ronak Harkhani on 23/06/24
 */
@Database(
    entities = [
        Album::class,
        Folder::class,
        Wallpaper::class,
    ],
    version = DB_VERSION,
)
@TypeConverters(
    StringListConverter::class,
    IntListConverter::class
)
abstract class WallmaticDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "WallmaticDatabase"
        const val DB_VERSION = 1

        /**
         * Create a new instance of [WallmaticDatabase]
         * Must be called only once. Should be called from DI.
         */
        fun create(appContext: Context): WallmaticDatabase =
            Room.databaseBuilder(
                context = appContext,
                klass = WallmaticDatabase::class.java,
                name = DB_NAME
            ).build()
    }

    abstract fun albumDao(): WallmaticDao

}
