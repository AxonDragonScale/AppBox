package com.axondragonscale.npinner.data.converter

import androidx.room.TypeConverter
import com.axondragonscale.npinner.model.ScheduleType

/**
 * Created by Ronak Harkhani on 30/04/23
 */
class ScheduleTypeConverter {

    @TypeConverter
    fun toScheduleType(type: Int): ScheduleType = ScheduleType.fromOrdinal(type)

    @TypeConverter
    fun fromScheduleType(type: ScheduleType): Int = type.ordinal

}
