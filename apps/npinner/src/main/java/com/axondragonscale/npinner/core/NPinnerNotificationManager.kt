package com.axondragonscale.npinner.core

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationManagerCompat
import com.axondragonscale.npinner.R
import com.axondragonscale.npinner.model.NPinnerNotification

/**
 * Created by Ronak Harkhani on 01/05/23
 */

private const val CHANNEL_ID = "NPinner_Pinned_Notifications_Channel"
private const val CHANNEL_NAME = "Pinned Notifications"
private const val CHANNEL_DESC = "Channel for Pinned Notifications"

class NPinnerNotificationManager(
    private val context: Context,
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    @SuppressLint("MissingPermission")
    fun postNotification(notification: NPinnerNotification) {
        val systemNotification = notification.toSystemNotification()
        notificationManager.notify(notification.id.hashCode(), systemNotification)
    }

    fun dismissNotification(id: String) {
        notificationManager.cancel(id.hashCode())
    }

    private fun NPinnerNotification.toSystemNotification(): Notification {
        val contentIntent = IntentProvider.getNotificationEditorPendingIntent(context, id)
        val unpinIntent = IntentProvider.getUnpinPendingIntent(context, id)
        val deleteIntent = IntentProvider.getDeletePendingIntent(context, id)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_npinner_icon)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(BigTextStyle().bigText(description))
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_pin, "Unpin", unpinIntent)
            .addAction(android.R.drawable.ic_menu_delete, "Delete", deleteIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        // Notification Channels are available since Oreo
        // Use Compat version to automatically handle it
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(CHANNEL_NAME)
            .setDescription(CHANNEL_DESC)
            .setShowBadge(false)
            .setLightsEnabled(false)
            .build()

        notificationManager.createNotificationChannel(channel)
    }
}
