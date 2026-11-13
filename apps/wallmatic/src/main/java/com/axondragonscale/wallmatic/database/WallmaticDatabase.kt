package com.axondragonscale.wallmatic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.axondragonscale.commons.room.IntListConverter
import com.axondragonscale.commons.room.StringListConverter
import com.axondragonscale.wallmatic.database.WallmaticDatabase.Companion.DB_VERSION
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
        const val DB_VERSION = 2

        /**
         * Create a new instance of [WallmaticDatabase]
         * Must be called only once. Should be called from DI.
         */
        fun create(appContext: Context): WallmaticDatabase =
            Room.databaseBuilder(
                context = appContext,
                klass = WallmaticDatabase::class.java,
                name = DB_NAME
            ).addMigrations(MIGRATION_1_2)
                .build()
    }

    abstract fun albumDao(): WallmaticDao

}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE wallpaper ADD COLUMN cropFocalX REAL NOT NULL DEFAULT 0.5")
        db.execSQL("ALTER TABLE wallpaper ADD COLUMN cropFocalY REAL NOT NULL DEFAULT 0.5")
        db.execSQL("ALTER TABLE wallpaper ADD COLUMN cropZoom REAL NOT NULL DEFAULT 1.0")
    }
}
