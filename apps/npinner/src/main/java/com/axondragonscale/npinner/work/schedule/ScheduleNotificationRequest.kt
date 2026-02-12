package com.axondragonscale.npinner.work.schedule

import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.axondragonscale.npinner.model.Schedule
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.util.concurrent.TimeUnit

/**
 * Created by Ronak Harkhani on 01/05/23
 */
object ScheduleNotificationRequest {

    const val INPUT_DATA_KEY_ID = "notificationId"

    fun getRequest(id: String, schedule: Schedule): OneTimeWorkRequest {
        val scheduledAt = schedule.asLocalDateTime
        val tz = TimeZone.currentSystemDefault()
        val scheduledInstant = scheduledAt.toInstant(tz)
        val currentInstant = Clock.System.now()
        val initialDelay = scheduledInstant - currentInstant

        val inputData = workDataOf(INPUT_DATA_KEY_ID to id)

        return OneTimeWorkRequestBuilder<ScheduleWorker>()
            .setInputData(inputData)
            .setInitialDelay(initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .addTag(getTag(id))
            .build()
    }

    private const val TAG_PREFIX = "npinner.work.schedule"
    fun getTag(id: String) = "$TAG_PREFIX.$id"

}
