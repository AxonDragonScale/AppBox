package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.commons.logW
import com.axondragonscale.wallmatic.core.WallpaperUpdateResult
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import com.axondragonscale.wallmatic.model.Config
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.util.failedTargetOrNull
import com.axondragonscale.wallmatic.util.isAutoCycleEnabled
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

/**
 * Owns the auto-cycle rule: change the wallpaper once `lastUpdated + updateInterval` has passed,
 * then leave exactly one wake-up armed. The schedule is derived state, never a chain of enqueued
 * work, so every trigger can converge on the correct state independently.
 */
class UpdateCoordinator(
    context: Context,
    private val appPrefsRepository: AppPrefsRepository,
    private val wallpaperUpdater: WallpaperUpdater,
    private val tickScheduler: TickScheduler,
) {

    companion object {
        private const val SYNC_WORK_NAME = "wallpaper_sync"
        private const val FALLBACK_WORK_NAME = "wallpaper_fallback"

        private val MIN_RETRY_DELAY_MILLIS = TimeUnit.MINUTES.toMillis(1)
        val MAX_RETRY_DELAY_MILLIS = TimeUnit.MINUTES.toMillis(5)
    }

    private val mutex = Mutex()
    private val workManager = WorkManager.getInstance(context)

    /** Applies the cycle if it is due — or the recorded failed target — then re-arms. */
    suspend fun reconcile() = mutex.withLock {
        val failedTarget = appPrefsRepository.configFlow.firstOrNull()?.failedTargetOrNull
        val result = if (failedTarget != null) {
            wallpaperUpdater.updateWallpaper(failedTarget)
        } else {
            wallpaperUpdater.updateWallpaper()
        }
        armAfter(result)
    }

    /** Applies immediately, bypassing the interval, then re-arms from the new `lastUpdated`. */
    suspend fun applyNow(target: TargetScreen) = mutex.withLock {
        armAfter(wallpaperUpdater.updateWallpaper(target))
    }

    /** Re-arms after the interval, album or auto-cycle toggles changed. */
    suspend fun reschedule() = mutex.withLock { armNext() }

    private suspend fun armAfter(result: WallpaperUpdateResult) {
        when (result) {
            WallpaperUpdateResult.Updated -> {
                this.logD { "Applied. Scheduling the next tick" }
                enqueueSync()
                armNext()
            }
            WallpaperUpdateResult.NotDue -> armNext()
            is WallpaperUpdateResult.Failed -> {
                this.logE { "Failed, retrying. cause: ${result.cause.message}" }
                enqueueSync()
                armRetry()
            }
            is WallpaperUpdateResult.Skipped -> {
                this.logW { "Skipped, waiting for the next cycle. reason: ${result.reason}" }
                armNextCycle()
            }
        }
    }

    private suspend fun armNext() {
        val config = schedulableConfig() ?: return tickScheduler.cancel()
        arm(config.nextUpdate)
    }

    private suspend fun armRetry() {
        val config = schedulableConfig() ?: return tickScheduler.cancel()
        val delay = config.updateInterval.coerceIn(MIN_RETRY_DELAY_MILLIS, MAX_RETRY_DELAY_MILLIS)
        arm(System.currentTimeMillis() + delay)
    }

    /**
     * A skip leaves `lastUpdated` untouched, so `nextUpdate` is already in the past and arming it
     * would tick again immediately, forever. A skip is a condition only the user can clear (album
     * deleted, every wallpaper blacklisted), so spend the wake-up on the next full interval.
     */
    private suspend fun armNextCycle() {
        val config = schedulableConfig() ?: return tickScheduler.cancel()
        arm(System.currentTimeMillis() + config.updateInterval)
    }

    private fun arm(triggerAtMillis: Long) {
        ensureFallbackScheduled()
        tickScheduler.armAt(triggerAtMillis)
    }

    private suspend fun schedulableConfig(): Config? =
        appPrefsRepository.configFlow.firstOrNull()?.takeIf { it.isInit && it.isAutoCycleEnabled }

    private fun enqueueSync() {
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<SyncWorker>().build(),
        )
    }

    /**
     * A periodic job evaluating the same rule. WorkManager persists its state, so it survives the
     * process death, reboot and app update that can silently drop the alarm.
     */
    private fun ensureFallbackScheduled() {
        workManager.enqueueUniquePeriodicWork(
            FALLBACK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<FallbackWorker>(15, TimeUnit.MINUTES).build(),
        )
    }

}
