package com.axondragonscale.limitless.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
 * - WEEKLY: Uses [time] and [daysOfWeekBitmask] (bitmask of ISO days: bit 0 = Mon .. bit 6 = Sun).
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
    val daysOfWeekBitmask: Int? = null,
    val dayOfMonth: Int? = null,
) {
    companion object {
        fun new(habitId: Long, type: ReminderType, time: LocalTime) = ReminderEntity(
            habitId = habitId,
            type = type,
            time = time,
        )

        /**
         * Converts an ISO day number (1=Monday .. 7=Sunday) to its bitmask value.
         */
        fun dayToBit(isoDayNumber: Int): Int = 1 shl (isoDayNumber - 1)
    }
}

/**
 * Checks if the given ISO day number (1=Mon .. 7=Sun) is set in the bitmask.
 */
fun Int.isDaySelected(isoDayNumber: Int): Boolean =
    this and ReminderEntity.dayToBit(isoDayNumber) != 0

/**
 * Toggles the given ISO day number in the bitmask and returns the new value.
 */
fun Int.toggleDay(isoDayNumber: Int): Int =
    this xor ReminderEntity.dayToBit(isoDayNumber)

/**
 * Returns all selected ISO day numbers (1=Mon .. 7=Sun) from the bitmask, sorted.
 */
fun Int.selectedDays(): List<Int> =
    (1..7).filter { this.isDaySelected(it) }
