package com.axondragonscale.limitless.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axondragonscale.limitless.database.LimitlessDatabase.Companion.DB_VERSION
import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.HabitDayCountDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.converter.LocalDateConverter
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.database.view.HabitDayCountView

/**
 * Created by Ronak Harkhani on 01/11/24
 */
@Database(
    entities = [
        HabitEntity::class,
        RecordEntity::class,
    ],
    views = [
        HabitDayCountView::class,
    ],
    version = DB_VERSION,
    exportSchema = true,
)
@TypeConverters(LocalDateConverter::class)
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
    abstract fun habitDayCountDao(): HabitDayCountDao

}
