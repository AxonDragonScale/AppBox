package com.axondragonscale.limitless.core

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.axondragonscale.limitless.LimitlessActivity
import com.axondragonscale.limitless.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Manages the notification channel and posting of habit reminder notifications.
 */
class ReminderNotificationManager(
    private val context: Context,
) {

    companion object {
        private const val CHANNEL_ID = "LIMITLESS_HABIT_REMINDERS_CHANNEL"
        private const val CHANNEL_NAME = "Habit Reminders"
        private const val CHANNEL_DESC = "Reminders for your tracked habits"

        const val EXTRA_HABIT_ID = "extra_habit_id"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    /**
     * Posts a reminder notification for the given habit.
     *
     * @param reminderId Unique ID for this reminder, used as the notification ID.
     * @param habitId The habit's ID, passed to the content intent for navigation.
     * @param habitName The habit's name, displayed as the notification title.
     */
    @SuppressLint("MissingPermission")
    fun postReminder(reminderId: Long, habitId: Long, habitName: String) {
        val notification = buildNotification(habitId, habitName)
        notificationManager.notify(reminderId.toInt(), notification)
    }

    fun dismiss(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt())
    }

    private fun buildNotification(habitId: Long, habitName: String): Notification {
        val contentIntent = createContentIntent(habitId)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.limitless_icon)
            .setContentTitle(habitName)
            .setContentText("Time to record your habit!")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
    }

    /**
     * Creates a PendingIntent that opens the app when the notification is tapped.
     */
    private fun createContentIntent(habitId: Long): PendingIntent {
        val intent = Intent(context, LimitlessActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        val flags =  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, habitId.toInt(), intent, flags)
    }

    private fun createNotificationChannel() = CoroutineScope(Dispatchers.IO).launch {
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(CHANNEL_NAME)
            .setDescription(CHANNEL_DESC)
            .setShowBadge(true)
            .build()

        notificationManager.createNotificationChannel(channel)
    }
}
