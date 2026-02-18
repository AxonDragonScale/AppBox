package com.axondragonscale.commons.room

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 01/07/24
 */
object IntListConverter {

    @TypeConverter
    fun toString(value: List<Int>): String = Json.encodeToString(value)

    @TypeConverter
    fun fromString(value: String): List<Int> = Json.decodeFromString(value)
}