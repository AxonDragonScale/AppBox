package com.axondragonscale.limitless.core

import com.axondragonscale.limitless.database.entity.ReminderEntity

actual class ReminderScheduler {
    actual fun schedule(reminder: ReminderEntity) {}
    actual fun cancel(reminderId: Long, habitId: Long) {}
    actual suspend fun rescheduleAll() {}
}
