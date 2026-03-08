package com.axondragonscale.limitless.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.axondragonscale.commons.room.LocalDateConverter
import com.axondragonscale.commons.room.LocalTimeConverter
import com.axondragonscale.limitless.database.LimitlessDatabase.Companion.DB_VERSION
import com.axondragonscale.limitless.database.converter.GoalTypeConverter
import com.axondragonscale.limitless.database.converter.HabitColorConverter
import com.axondragonscale.limitless.database.converter.HabitViewConverter
import com.axondragonscale.limitless.database.converter.ReminderTypeConverter
import com.axondragonscale.limitless.database.dao.GoalDao
import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.HabitDayCountDao
import com.axondragonscale.limitless.database.dao.HabitHourCountDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.dao.ReminderDao
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.database.view.HabitDayCountView
import com.axondragonscale.limitless.database.view.HabitHourCountView

/**
 * Created by Ronak Harkhani on 01/11/24
 */
@Database(
    entities = [
        HabitEntity::class,
        RecordEntity::class,
        ReminderEntity::class,
        GoalEntity::class,
    ],
    views = [
        HabitDayCountView::class,
        HabitHourCountView::class,
    ],
    version = DB_VERSION,
    exportSchema = true,
)
@TypeConverters(
    LocalDateConverter::class,
    LocalTimeConverter::class,
    ReminderTypeConverter::class,
    GoalTypeConverter::class,
    HabitColorConverter::class,
    HabitViewConverter::class,
)
@ConstructedBy(LimitlessDatabaseConstructor::class)
abstract class LimitlessDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "limitless"
        const val DB_VERSION = 3
    }

    abstract fun habitDao(): HabitDao
    abstract fun recordDao(): RecordDao
    abstract fun habitDayCountDao(): HabitDayCountDao
    abstract fun habitHourCountDao(): HabitHourCountDao
    abstract fun reminderDao(): ReminderDao
    abstract fun goalDao(): GoalDao

}

// The KSP-generated implementation of this is suppressed because Room generates it.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object LimitlessDatabaseConstructor : RoomDatabaseConstructor<LimitlessDatabase> {
    override fun initialize(): LimitlessDatabase
}
