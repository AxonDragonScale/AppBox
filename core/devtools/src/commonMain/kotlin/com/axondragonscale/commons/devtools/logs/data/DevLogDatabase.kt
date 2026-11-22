package com.axondragonscale.commons.devtools.logs.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/** Created by Ronak Harkhani on 11/21/26 */
internal const val DEV_LOG_DB_NAME = "dev_logs.db"

@Database(entities = [DevLogEntity::class], version = 1, exportSchema = false)
@ConstructedBy(DevLogDatabaseConstructor::class)
internal abstract class DevLogDatabase : RoomDatabase() {
    abstract fun logDao(): DevLogDao
}

// Room generates the actual for every non-Android target.
internal expect object DevLogDatabaseConstructor : RoomDatabaseConstructor<DevLogDatabase>
