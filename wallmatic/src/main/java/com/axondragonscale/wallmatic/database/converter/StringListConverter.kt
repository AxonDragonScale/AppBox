package com.axondragonscale.wallmatic.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 26/06/24
 */
object StringListConverter {

    private val json = Json

    @TypeConverter
    fun toString(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun fromString(value: String): List<String> = json.decodeFromString(value)

}
