package com.axondragonscale.npinner.work.monitor

import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

/**
 * Created by Ronak Harkhani on 15/05/23
 */
object MonitorRequest {
    
    private const val MONITOR_INTERVAL = 30L
    const val MONITOR_TAG = "npinner.work.monitor"
    
    fun getRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<MonitorWorker>(
            repeatInterval = MONITOR_INTERVAL,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .addTag(MONITOR_TAG)
            .build()
    }
    
}
