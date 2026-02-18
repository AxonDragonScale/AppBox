package com.axondragonscale.limitless.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.limitless.model.ReminderType
import com.axondragonscale.limitless.repository.LimitlessRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * BroadcastReceiver that fires when an exact alarm triggers for a habit reminder.
 * - Fetches the reminder and its associated habit from the database.
 * - Posts a system notification via [ReminderNotificationManager].
 * - For recurring reminders, reschedules the next alarm via [ReminderScheduler].
 * - For one-time reminders, deletes the reminder from the database after firing.
 */
class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_REMINDER = "com.axondragonscale.limitless.ACTION_REMINDER"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }

    private val repository: LimitlessRepository by inject()
    private val scheduler: ReminderScheduler by inject()
    private val notificationManager: ReminderNotificationManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (reminderId == -1L || habitId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = repository.getReminder(reminderId) ?: return@launch
                val habit = repository.getHabit(habitId) ?: return@launch

                // Post the notification
                notificationManager.postReminder(
                    reminderId = reminderId,
                    habitId = habitId,
                    habitName = habit.name,
                )

                // Handle recurring vs one-time
                when (reminder.type) {
                    ReminderType.ONE_TIME -> repository.deleteReminder(reminderId) // One-time reminder has fired, delete it

                    ReminderType.DAILY,
                    ReminderType.WEEKLY,
                    ReminderType.MONTHLY -> scheduler.schedule(reminder) // Schedule the next occurrence
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
