package com.axondragonscale.limitless.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.axondragonscale.limitless.model.BitWeek
import com.axondragonscale.limitless.model.ReminderType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Represents a reminder for a habit.
 * Each habit can have multiple reminders.
 * A reminder can be one-time or recurring (daily, weekly, monthly).
 *
 * - ONE_TIME: Uses [date] and [time] to fire once.
 * - DAILY: Uses [time] only, fires every day.
 * - WEEKLY: Uses [time] and [daysOfWeek] (bitmask of ISO days: bit 0 = Mon .. bit 6 = Sun).
 * - MONTHLY: Uses [time] and [dayOfMonth] (1-31, clamped to month length).
 */
@Entity(
    tableName = "reminder",
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
    ],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val habitId: Long,
    val type: ReminderType,
    val time: LocalTime,
    val date: LocalDate? = null,
    val daysOfWeek: BitWeek? = null,
    val dayOfMonth: Int? = null,
) {
    companion object {
        fun new(habitId: Long, type: ReminderType, time: LocalTime) = ReminderEntity(
            habitId = habitId,
            type = type,
            time = time,
        )
    }
}
