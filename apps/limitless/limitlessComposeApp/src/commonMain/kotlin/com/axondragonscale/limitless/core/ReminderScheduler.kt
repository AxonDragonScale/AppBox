package com.axondragonscale.limitless.core

import com.axondragonscale.limitless.database.entity.ReminderEntity

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Platform-specific scheduler for habit reminders.
 * Android: uses AlarmManager for exact alarms.
 * Desktop / iOS: no-op stubs (to be implemented later).
 */
expect class ReminderScheduler {
    fun schedule(reminder: ReminderEntity)
    fun cancel(reminderId: Long, habitId: Long)
    suspend fun rescheduleAll()
}
