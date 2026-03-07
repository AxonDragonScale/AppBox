package com.axondragonscale.commons.room

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 26/06/24
 */
object StringListConverter {

    @TypeConverter
    fun toString(value: List<String>): String = Json.Default.encodeToString(value)

    @TypeConverter
    fun fromString(value: String): List<String> = Json.Default.decodeFromString(value)

}