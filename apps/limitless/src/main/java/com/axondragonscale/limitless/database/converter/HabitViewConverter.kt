// Created by Ronak Harkhani on 04/03/26

package com.axondragonscale.limitless.database.converter

import androidx.room.TypeConverter
import com.axondragonscale.limitless.model.HabitView

object HabitViewConverter {

    @TypeConverter
    fun fromHabitView(view: HabitView?): Int? = view?.ordinal

    @TypeConverter
    fun toHabitView(ordinal: Int?): HabitView? = ordinal?.let { HabitView.fromOrdinal(it) }
}
