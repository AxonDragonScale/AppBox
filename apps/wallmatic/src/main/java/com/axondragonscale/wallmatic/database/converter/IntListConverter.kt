package com.axondragonscale.wallmatic.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 01/07/24
 */
object IntListConverter {

    private val json = Json

    @TypeConverter
    fun toString(value: List<Int>): String = json.encodeToString(value)

    @TypeConverter
    fun fromString(value: String): List<Int> = json.decodeFromString(value)
}
