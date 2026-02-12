package com.axondragonscale.npinner.data.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime

/**
 * Created by Ronak Harkhani on 30/04/23
 */
class LocalTimeTypeConverter {

    @TypeConverter
    fun toLocalTime(time: String): LocalTime = LocalTime.parse(time)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.toString()

}
