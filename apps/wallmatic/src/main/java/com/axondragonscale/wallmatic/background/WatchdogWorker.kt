package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.background.WallpaperWorkScheduler.Companion.WALLPAPER_WORK_NAME
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Periodic safety-net worker that revives the wallpaper update chain if it has died.
 *
 * The primary chain is a self-rescheduling [WallpaperWorker] (one-time work). It can die
 * silently due to force-stops, OEM task killers, or other edge cases not covered by
 * [WallmaticReceiver]. This worker runs on a 15-minute period and re-kicks the chain
 * whenever it finds no ENQUEUED or RUNNING work under [WALLPAPER_WORK_NAME].
 *
 * Uses [ExistingPeriodicWorkPolicy.KEEP] when enqueued, so re-enqueuing from multiple
 * call sites (app start, receiver) is always a safe no-op.
 */
class WatchdogWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val scheduler: WallpaperWorkScheduler by inject()

    override suspend fun doWork(): Result {
        this.logD { "doWork start" }

        val workInfos = WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkFlow(WALLPAPER_WORK_NAME)
            .first()

        val isChainAlive = workInfos.any {
            it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
        }

        if (isChainAlive) {
            this.logD { "doWork: chain is alive, nothing to do" }
        } else {
            this.logD { "doWork: chain is dead, rescheduling" }
            scheduler.scheduleNextUpdate()
        }

        return Result.success()
    }

}
