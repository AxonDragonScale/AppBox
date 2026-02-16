package com.axondragonscale.limitless.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.axondragonscale.limitless.database.LimitlessDatabase.Companion.DB_VERSION
import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity

/**
 * Created by Ronak Harkhani on 01/11/24
 */
@Database(
    entities = [
        HabitEntity::class,
        RecordEntity::class,
    ],
    version = DB_VERSION,
    exportSchema = true,
)
abstract class LimitlessDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "limitless"
        const val DB_VERSION = 1

        /**
         * Create a new instance of [LimitlessDatabase]
         * Must be called only once. Should be called from DI.
         */
        fun create(context: Context): LimitlessDatabase =
            Room.databaseBuilder(
                context = context,
                klass = LimitlessDatabase::class.java,
                name = DB_NAME
            ).build()
    }

    abstract fun habitDao(): HabitDao
    abstract fun recordDao(): RecordDao

}
