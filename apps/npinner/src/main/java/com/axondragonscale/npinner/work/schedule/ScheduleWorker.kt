package com.axondragonscale.npinner.work.schedule

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.axondragonscale.npinner.core.NPinnerNotificationManager
import com.axondragonscale.npinner.model.NPinnerNotification
import com.axondragonscale.npinner.model.ScheduleType
import com.axondragonscale.npinner.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Ronak Harkhani on 01/05/23
 */

class ScheduleWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: NotificationRepository,
    private val notificationManager: NPinnerNotificationManager,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getString(ScheduleNotificationRequest.INPUT_DATA_KEY_ID)
        if (id.isNullOrBlank()) return Result.failure()

        withContext(Dispatchers.IO) {
            repository.updatePinStatus(id, true) // Update Pin Status

            val notification = repository.getNotification(id)
            notificationManager.postNotification(notification)

            rescheduleIfNeeded(notification)
        }

        return Result.success()
    }

    private suspend fun rescheduleIfNeeded(notification: NPinnerNotification) {
        if (notification.schedule?.type == null) return
        val schedule = notification.schedule

        val currentScheduledTime = schedule.asLocalDateTime
        val newScheduleTime = when (schedule.type!!) {
            ScheduleType.DAY -> currentScheduledTime.plusDays(1)
            ScheduleType.WEEK -> currentScheduledTime.plusWeeks(1)
            ScheduleType.MONTH -> currentScheduledTime.plusMonths(1)
        }

        val updatedNotification = notification.copy(
            schedule = schedule.copy(
                date = newScheduleTime.toLocalDate(),
                time = newScheduleTime.toLocalTime()
            )
        )

        repository.upsert(updatedNotification)

        with(updatedNotification) {
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    ScheduleNotificationRequest.getTag(id),
                    ExistingWorkPolicy.REPLACE,
                    ScheduleNotificationRequest.getRequest(id, schedule),
                )
        }
    }

}
