package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.ui.util.toTimestampString
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

/**
 * Created by Ronak Harkhani on 22/09/24
 */
class WallpaperWorkScheduler(
    private val context: Context,
    private val appPrefsRepository: AppPrefsRepository,
) {

    companion object {
        const val WALLPAPER_WORK_NAME = "wallpaper_update"
    }

    private val workManager = WorkManager.getInstance(context)

    suspend fun scheduleNextUpdate() {
        this.logD { "scheduleNextUpdate start" }
        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logD { "schedule skipped. Config is null" }
            return
        }

        if (!config.isInit) {
            this.logD { "schedule skipped. isInit: ${config.isInit}" }
            return
        }

        if (!config.homeConfig.autoCycleEnabled && !config.lockConfig.autoCycleEnabled) {
            this.logD { "schedule skipped. Autocycle disabled" }
            return
        }

        val now = System.currentTimeMillis()
        val nextUpdate = config.nextUpdate.coerceAtLeast(now)
        val delay = nextUpdate - now

        this.logD { "Scheduling next update: ${nextUpdate.toTimestampString()} (delay: ${delay}ms)" }
        val request = OneTimeWorkRequestBuilder<WallpaperWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(WALLPAPER_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        this.logD { "Scheduled update successfully" }
    }

    fun cancelScheduledUpdates() {
        this.logD { "Cancelling scheduled updates" }
        workManager.cancelUniqueWork(WALLPAPER_WORK_NAME)
    }

}
