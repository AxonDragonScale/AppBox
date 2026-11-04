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
 * The primary chain is a self-appending [WallpaperWorker] (one-time work). It can die
 * silently due to force-stops, OEM task killers, or other edge cases not covered by
 * [WallmaticReceiver]. This worker runs on a 15-minute period and re-kicks the chain
 * whenever it finds no ENQUEUED, RUNNING or BLOCKED work under [WALLPAPER_WORK_NAME].
 *
 * [WorkInfo.State.BLOCKED] counts as alive because the successor a running [WallpaperWorker]
 * appends is its dependent, and dependents sit in BLOCKED until the run finishes. Without it the
 * watchdog would see "chain is dead" during every run and replace the chain, cancelling the very
 * run it is supposed to protect.
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
            it.state == WorkInfo.State.ENQUEUED ||
                it.state == WorkInfo.State.RUNNING ||
                it.state == WorkInfo.State.BLOCKED
        }

        if (isChainAlive) {
            this.logD { "doWork: chain is alive, nothing to do" }
        } else {
            this.logD { "doWork: chain is dead, ensuring a scheduled update" }
            scheduler.ensureScheduled()
        }

        return Result.success()
    }

}
