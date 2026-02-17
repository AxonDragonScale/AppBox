package com.axondragonscale.limitless.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Room TypeConverter for kotlinx.datetime.LocalTime.
 * Converts between LocalTime and String (ISO-8601 format: HH:MM:SS).
 */
object LocalTimeConverter {

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? = timeString?.let { LocalTime.parse(it) }
}
