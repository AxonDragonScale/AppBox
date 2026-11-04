package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.model.Config
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.ui.util.toTimestampString
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

/**
 * Schedules the auto-cycle's one-time [WallpaperWorker] chain.
 *
 * The chain is deliberately built from one-time work instead of a single periodic worker:
 * `PeriodicWorkRequest` has a 15-minute minimum interval and runs anywhere inside a 5-minute flex
 * window, so a tick can land just *before* the due moment and be skipped by
 * `WallpaperUpdater.updateWallpaper()`'s due check — a 15-minute interval would then change the
 * wallpaper every ~20–30 minutes. One-time work with an explicit delay keeps 15/30/45/60 minute
 * intervals exact, and is what makes the dev 1-minute fast cycle possible.
 *
 * Every method below goes through [enqueueUpdate], so the config read, the guards and the work
 * name live in one place; only the [ExistingWorkPolicy] and the delay differ.
 *
 * Created by Ronak Harkhani on 22/09/24
 */
class WallpaperWorkScheduler(
    private val context: Context,
    private val appPrefsRepository: AppPrefsRepository,
) {

    companion object {
        const val WALLPAPER_WORK_NAME = "wallpaper_update"
        const val WATCHDOG_WORK_NAME = "wallpaper_watchdog"

        /** Upper bound for the delay before a failed cycle is retried. */
        val RETRY_DELAY_MILLIS = TimeUnit.MINUTES.toMillis(5)

        /** Lower bound for the delay before a failed cycle is retried. */
        private val MIN_RETRY_DELAY_MILLIS = TimeUnit.MINUTES.toMillis(1)
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Extends the chain with the next scheduled update. Called by [WallpaperWorker] from inside
     * `doWork()`, after a cycle that completed (`Updated` / `NotDue`).
     *
     * Uses [ExistingWorkPolicy.APPEND_OR_REPLACE]: the successor is appended as a dependent of the
     * run that is calling this, so enqueuing never cancels that run. [ExistingWorkPolicy.REPLACE]
     * here would cancel the very work that is executing this call.
     */
    suspend fun appendNextUpdate() {
        this.logD { "appendNextUpdate start" }
        enqueueUpdate(ExistingWorkPolicy.APPEND_OR_REPLACE) { config -> nextUpdateDelay(config) }
    }

    /**
     * Extends the chain with a bounded retry. Called by [WallpaperWorker] from inside `doWork()`,
     * after a cycle that applied nothing (`Failed` / `Skipped`) or threw.
     *
     * A cycle that applied nothing does not advance `lastUpdated`, so [appendNextUpdate] would
     * compute a negative delay and clamp it to zero — the chain would hot-loop. WorkManager's own
     * retry backoff is no better: it can stretch to hours, which is useless for a 15-minute
     * cadence. Retrying on our own schedule keeps the delay bounded to [RETRY_DELAY_MILLIS].
     *
     * Appends rather than replaces for the same reason as [appendNextUpdate].
     */
    suspend fun appendRetry() {
        this.logD { "appendRetry start" }
        enqueueUpdate(ExistingWorkPolicy.APPEND_OR_REPLACE) { config -> retryDelay(config) }
    }

    /**
     * Makes sure one pending update exists, without disturbing an in-flight run or resetting the
     * delay of an already pending one.
     *
     * Uses [ExistingWorkPolicy.KEEP]. For callers that only have to establish the chain — app start
     * and [WallmaticReceiver]. Opening the app must not cancel or push back an update that is
     * already queued or currently running. When nothing is pending (fresh install, wiped
     * WorkManager state) this enqueues the update with the normal delay computation.
     */
    suspend fun ensureScheduled() {
        this.logD { "ensureScheduled start" }
        enqueueUpdate(ExistingWorkPolicy.KEEP) { config -> nextUpdateDelay(config) }
    }

    /**
     * Recomputes and replaces the pending update. For callers whose inputs actually changed — the
     * album, mirroring, auto-cycle toggles and the interval in `HomeVM` — where the previously
     * enqueued delay is now wrong and must be discarded.
     *
     * Uses [ExistingWorkPolicy.REPLACE]. Only safe outside the worker: replacing cancels whatever
     * is pending *or running* under [WALLPAPER_WORK_NAME].
     */
    suspend fun reschedule() {
        this.logD { "reschedule start" }
        enqueueUpdate(ExistingWorkPolicy.REPLACE) { config -> nextUpdateDelay(config) }
    }

    /**
     * Delay until the next due update: `config.nextUpdate - now`, clamped at 0 so an overdue chain
     * runs immediately. The worker appends right after writing `lastUpdated`, so the delay is
     * derived from the cycle that just finished.
     */
    private fun nextUpdateDelay(config: Config): Long {
        val now = System.currentTimeMillis()
        val nextUpdate = config.nextUpdate.coerceAtLeast(now)
        this.logD { "Scheduling next update: ${nextUpdate.toTimestampString()} (delay: ${nextUpdate - now}ms)" }
        return nextUpdate - now
    }

    /** Delay before retrying a cycle that applied nothing: `config.updateInterval` clamped to
     * `[1 min, RETRY_DELAY_MILLIS]`. */
    private fun retryDelay(config: Config): Long {
        val delay = config.updateInterval.coerceIn(MIN_RETRY_DELAY_MILLIS, RETRY_DELAY_MILLIS)
        this.logD { "Scheduling retry: delay: ${delay}ms" }
        return delay
    }

    /**
     * Enqueues [WallpaperWorker] with [policy] and the delay [delayMillis] computes, unless
     * auto-cycle is not configured. Shared by every scheduling entry point, so the guards and the
     * work identity stay in one place and only the policy (and delay) differ per caller.
     */
    private suspend fun enqueueUpdate(
        policy: ExistingWorkPolicy,
        delayMillis: (Config) -> Long,
    ) {
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

        val delay = delayMillis(config)
        val request = OneTimeWorkRequestBuilder<WallpaperWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(WALLPAPER_WORK_NAME, policy, request)
        this.logD { "Scheduled update successfully with policy $policy" }
    }

    fun cancelScheduledUpdates() {
        this.logD { "Cancelling scheduled updates" }
        workManager.cancelUniqueWork(WALLPAPER_WORK_NAME)
    }

    /**
     * Enqueues a periodic watchdog that revives the one-time update chain if it ever goes dead.
     * Uses KEEP policy so repeated calls (app start, receiver) are no-ops when already scheduled.
     */
    fun scheduleWatchdog() {
        this.logD { "Scheduling watchdog" }
        val request = PeriodicWorkRequestBuilder<WatchdogWorker>(15, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WATCHDOG_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

}
