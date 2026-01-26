package com.axondragonscale.appdump.jest.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.appdump.jest.model.JokeType

/**
 * Created by Ronak Harkhani on 03/04/24
 */
object JokeTypeConverter {

    @TypeConverter
    fun toJokeType(ordinal: Int) = JokeType.fromOrdinal(ordinal)

    @TypeConverter
    fun fromJokeType(type: JokeType) = type.ordinal

}
