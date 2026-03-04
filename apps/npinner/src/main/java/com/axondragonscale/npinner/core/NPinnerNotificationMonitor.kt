package com.axondragonscale.npinner.core

import android.content.Context
import com.axondragonscale.commons.notificationManager
import com.axondragonscale.npinner.repository.NotificationRepository

/**
 * Created by Ronak Harkhani on 01/05/23
 */

class NPinnerNotificationMonitor(
    private val context: Context,
    private val repository: NotificationRepository,
    private val notificationManager: NPinnerNotificationManager,
) {

    suspend fun ensurePinnedNotificationVisibility() {
        val pinnedNotifications = repository.getPinnedNotifications()
        val activeNotifications = context.notificationManager?.activeNotifications ?: return

        pinnedNotifications.forEach { notification ->
            val isVisible = activeNotifications.any { it.id == notification.id.hashCode() }
            if (!isVisible) notificationManager.postNotification(notification)
        }
    }

}
