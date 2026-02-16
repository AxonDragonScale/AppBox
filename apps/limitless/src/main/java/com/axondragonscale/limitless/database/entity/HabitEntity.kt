package com.axondragonscale.limitless.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 01/11/24
 */
@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val name: String,
    val description: String,
) {
    companion object {
        fun new() = HabitEntity(
            name = "",
            description = ""
        )
    }
}
