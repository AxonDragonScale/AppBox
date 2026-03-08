package com.axondragonscale.limitless.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.limitless.model.HabitColor

/**
 * Room TypeConverter for [HabitColor].
 * Converts between HabitColor and its name String for Room storage.
 * Storing by name (rather than ordinal) is resilient to enum reordering.
 */
object HabitColorConverter {

    @TypeConverter
    fun fromHabitColor(color: HabitColor?): String? = color?.name

    @TypeConverter
    fun toHabitColor(name: String?): HabitColor? = name?.let { HabitColor.fromName(it) }
}
