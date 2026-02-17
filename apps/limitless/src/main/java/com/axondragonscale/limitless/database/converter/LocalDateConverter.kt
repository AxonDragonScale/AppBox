package com.axondragonscale.limitless.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 10/01/25
 *
 * Room TypeConverter for kotlinx.datetime.LocalDate.
 * Converts between LocalDate and String (ISO-8601 format: YYYY-MM-DD).
 */
object LocalDateConverter {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }
}
