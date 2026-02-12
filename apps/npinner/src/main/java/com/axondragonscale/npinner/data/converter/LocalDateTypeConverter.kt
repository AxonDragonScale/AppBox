package com.axondragonscale.npinner.data.converter

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Created by Ronak Harkhani on 30/04/23
 */
class LocalDateTypeConverter {

    @TypeConverter
    fun toLocalDate(date: String): LocalDate = LocalDate.parse(date)

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

}
