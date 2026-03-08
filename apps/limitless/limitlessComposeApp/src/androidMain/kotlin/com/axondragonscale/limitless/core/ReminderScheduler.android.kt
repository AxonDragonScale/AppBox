package com.axondragonscale.limitless.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.axondragonscale.commons.canScheduleExactAlarms
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.model.ReminderType
import com.axondragonscale.limitless.repository.LimitlessRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Schedules and cancels exact alarms for habit reminders using [AlarmManager].
 * Each reminder gets a unique [PendingIntent] based on its ID.
 */
actual class ReminderScheduler(
    private val context: Context,
    private val repository: LimitlessRepository,
) {

    private val alarmManager = context.getSystemService<AlarmManager>()

    /**
     * Schedules an exact alarm for the given reminder.
     * If the computed next trigger time is in the past, the alarm is not scheduled.
     */
    actual fun schedule(reminder: ReminderEntity) {
        if (!context.canScheduleExactAlarms) return
        val triggerAtMillis = computeNextTriggerMillis(reminder) ?: return
        val pendingIntent = createPendingIntent(reminder.id, reminder.habitId)

        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    /**
     * Cancels a previously scheduled alarm for the given reminder ID.
     */
    actual fun cancel(reminderId: Long, habitId: Long) {
        val pendingIntent = createPendingIntent(reminderId, habitId)
        alarmManager?.cancel(pendingIntent)
    }

    /**
     * Reschedules all reminders from the database.
     * Useful after device reboot or app update.
     */
    actual suspend fun rescheduleAll() {
        val reminders = repository.getAllReminders()
        reminders.forEach { schedule(it) }
    }

    /**
     * Computes the next trigger time in epoch millis for the given reminder.
     * Returns null if the reminder is a one-time reminder whose date has already passed.
     */
    private fun computeNextTriggerMillis(reminder: ReminderEntity): Long? {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val nowLocal = now.toLocalDateTime(tz)

        val nextDateTime = when (reminder.type) {
            ReminderType.ONE_TIME -> {
                val date = reminder.date ?: return null
                val dt = date.atTime(reminder.time)
                // If the one-time reminder is in the past, don't schedule
                if (dt.toInstant(tz) <= now) return null
                dt
            }

            ReminderType.DAILY -> {
                val todayTrigger = nowLocal.date.atTime(reminder.time)
                if (todayTrigger.toInstant(tz) > now) {
                    todayTrigger
                } else {
                    nowLocal.date.plus(1, DateTimeUnit.DAY).atTime(reminder.time)
                }
            }

            ReminderType.WEEKLY -> {
                val bitWeek = reminder.daysOfWeek ?: return null
                val selectedDays = bitWeek.selectedDays()
                if (selectedDays.isEmpty()) return null
                computeNextWeeklyTrigger(nowLocal, reminder.time, selectedDays, tz, now)
            }

            ReminderType.MONTHLY -> {
                val targetDayOfMonth = reminder.dayOfMonth ?: return null
                computeNextMonthlyTrigger(nowLocal, reminder.time, targetDayOfMonth, tz, now)
            }
        }

        return nextDateTime.toInstant(tz).toEpochMilliseconds()
    }

    private fun computeNextWeeklyTrigger(
        nowLocal: LocalDateTime,
        time: LocalTime,
        selectedDays: List<Int>,
        tz: TimeZone,
        now: Instant,
    ): LocalDateTime {
        val currentDayOfWeek = nowLocal.date.dayOfWeek.isoDayNumber

        // Build candidates: for each selected day, compute the next occurrence
        // Try this week first, then next week if needed
        val candidates = selectedDays.map { targetDay ->
            var daysUntil = targetDay - currentDayOfWeek
            if (daysUntil < 0) daysUntil += 7

            val candidateDate = nowLocal.date.plus(daysUntil, DateTimeUnit.DAY)
            val candidateDateTime = candidateDate.atTime(time)

            if (candidateDateTime.toInstant(tz) > now) {
                candidateDateTime
            } else {
                // Same day but time has passed, schedule for next week
                candidateDate.plus(7, DateTimeUnit.DAY).atTime(time)
            }
        }

        // Return the earliest upcoming trigger
        return candidates.minBy { it.toInstant(tz).toEpochMilliseconds() }
    }

    private fun computeNextMonthlyTrigger(
        nowLocal: LocalDateTime,
        time: LocalTime,
        targetDayOfMonth: Int,
        tz: TimeZone,
        now: Instant,
    ): LocalDateTime {
        val clampedDay = clampDayOfMonth(nowLocal.date.year, nowLocal.date.month.number, targetDayOfMonth)
        val thisMonthDate = LocalDate(nowLocal.date.year, nowLocal.date.month.number, clampedDay)
        val thisMonthTrigger = thisMonthDate.atTime(time)

        if (thisMonthTrigger.toInstant(tz) > now) return thisMonthTrigger

        // Otherwise try next month
        val nextMonthDate = nowLocal.date.plus(1, DateTimeUnit.MONTH)
        val nextClampedDay = clampDayOfMonth(nextMonthDate.year, nextMonthDate.month.number, targetDayOfMonth)
        val nextDate = LocalDate(nextMonthDate.year, nextMonthDate.month.number, nextClampedDay)
        return nextDate.atTime(time)
    }

    /**
     * Clamps the target day to the number of days in the given month.
     * e.g. day 31 in February → 28 or 29.
     */
    private fun clampDayOfMonth(year: Int, month: Int, day: Int): Int {
        val daysInMonth = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> 31
        }
        return day.coerceAtMost(daysInMonth)
    }

    private fun createPendingIntent(reminderId: Long, habitId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderReceiver.EXTRA_HABIT_ID, habitId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, reminderId.toInt(), intent, flags)
    }
}
