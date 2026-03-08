package com.axondragonscale.jest.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.jest.model.JokeType

/**
 * Created by Ronak Harkhani on 03/04/24
 */
object JokeTypeConverter {

    @TypeConverter
    fun toJokeType(ordinal: Int) = JokeType.fromOrdinal(ordinal)

    @TypeConverter
    fun fromJokeType(type: JokeType) = type.ordinal

}
