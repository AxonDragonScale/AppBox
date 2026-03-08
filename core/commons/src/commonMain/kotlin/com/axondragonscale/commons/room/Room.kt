package com.axondragonscale.commons.room

import androidx.room.RoomDatabase

/**
 * Created by Ronak Harkhani on 08 March 2026.
 */

/**
 * Platform-specific factory function to create a Database.
 * Each platform provides its own Room builder configuration.
 */
expect inline fun <reified T: RoomDatabase> createDatabase(name: String): T
