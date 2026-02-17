package com.axondragonscale.limitless.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.limitless.model.ReminderType

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Room TypeConverter for [ReminderType].
 * Converts between ReminderType and its ordinal Int for Room storage.
 */
object ReminderTypeConverter {

    @TypeConverter
    fun fromReminderType(type: ReminderType?): Int? = type?.ordinal

    @TypeConverter
    fun toReminderType(ordinal: Int?): ReminderType? = ordinal?.let { ReminderType.fromOrdinal(it) }
}
