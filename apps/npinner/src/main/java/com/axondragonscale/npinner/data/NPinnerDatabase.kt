package com.axondragonscale.npinner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.axondragonscale.npinner.data.converter.LocalDateTypeConverter
import com.axondragonscale.npinner.data.converter.LocalTimeTypeConverter
import com.axondragonscale.npinner.data.converter.ScheduleTypeConverter
import com.axondragonscale.npinner.data.dao.NotificationDao
import com.axondragonscale.npinner.data.entity.NotificationEntity

/**
 * Created by Ronak Harkhani on 30/04/23
 */

@Database(
    entities = [NotificationEntity::class],
    views = [],
    version = 1,
    exportSchema = true,
    autoMigrations = [],
)
@TypeConverters(
    LocalDateTypeConverter::class,
    LocalTimeTypeConverter::class,
    ScheduleTypeConverter::class,
)
abstract class NPinnerDatabase : RoomDatabase() {

    companion object {
        private const val DB_NAME = "NPinnerDatabase"

        private var instance: NPinnerDatabase? = null
        fun getInstance(appContext: Context) = instance ?: synchronized(this) {
            instance = Room
                .databaseBuilder(appContext, NPinnerDatabase::class.java, DB_NAME)
                .build()
            instance!!
        }
    }

    abstract fun notificationDao(): NotificationDao

}
