// Created by Ronak Harkhani on 03/07/25

package com.axondragonscale.limitless.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.limitless.model.GoalType

/**
 * Room TypeConverter for [GoalType].
 * Converts between GoalType and its ordinal Int for Room storage.
 */
object GoalTypeConverter {

    @TypeConverter
    fun fromGoalType(type: GoalType?): Int? = type?.ordinal

    @TypeConverter
    fun toGoalType(ordinal: Int?): GoalType? = ordinal?.let { GoalType.fromOrdinal(it) }
}
