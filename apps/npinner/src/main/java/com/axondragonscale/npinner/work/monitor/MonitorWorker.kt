package com.axondragonscale.npinner.work.monitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.axondragonscale.npinner.core.NPinnerNotificationMonitor

/**
 * Created by Ronak Harkhani on 15/05/23
 */

class MonitorWorker(
    context: Context,
    params: WorkerParameters,
    private val notificationMonitor: NPinnerNotificationMonitor,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notificationMonitor.ensurePinnedNotificationVisibility()
        return Result.success()
    }

}
