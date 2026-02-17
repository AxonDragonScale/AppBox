package com.axondragonscale.limitless.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by Ronak Harkhani on 01/11/24
 */
@Entity(
    tableName = "record",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("habitId"),
        Index("timestamp"),
        Index(value = ["habitId", "timestamp"]), // Composite index for better query performance
    ],
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val habitId: Long,
    val timestamp: Long,
    val message: String,
) {
    companion object {
        fun new(habitId: Long, message: String) = RecordEntity(
            habitId = habitId,
            timestamp = System.currentTimeMillis(),
            message = message,
        )
    }
}
