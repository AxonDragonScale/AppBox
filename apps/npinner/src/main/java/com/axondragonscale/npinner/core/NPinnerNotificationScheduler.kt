package com.axondragonscale.npinner.core

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.axondragonscale.npinner.model.NPinnerNotification
import com.axondragonscale.npinner.work.schedule.ScheduleNotificationRequest
import java.time.LocalDateTime

/**
 * Created by Ronak Harkhani on 01/05/23
 */

class NPinnerNotificationScheduler(
    private val context: Context,
) {

    fun scheduleNotification(notification: NPinnerNotification) {
        with(notification) {
            schedule ?: return

            val scheduledAt = schedule.asLocalDateTime
            val currentTime = LocalDateTime.now()
            if (scheduledAt.isBefore(currentTime)) return

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    ScheduleNotificationRequest.getTag(id),
                    ExistingWorkPolicy.REPLACE,
                    ScheduleNotificationRequest.getRequest(id, schedule)
                )
        }
    }

    fun cancelScheduledNotification(id: String) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(ScheduleNotificationRequest.getTag(id))
    }

}
