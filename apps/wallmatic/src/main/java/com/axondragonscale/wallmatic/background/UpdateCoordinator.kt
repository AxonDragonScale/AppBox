package com.axondragonscale.wallmatic.background

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.commons.logW
import com.axondragonscale.wallmatic.core.AlbumSyncer
import com.axondragonscale.wallmatic.core.ConfigReconciler
import com.axondragonscale.wallmatic.core.WallpaperCycler
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.WallpaperUpdateResult
import com.axondragonscale.wallmatic.repository.ConfigRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

/**
 * Owns the auto-cycle rule: change the wallpaper once `lastUpdated + interval` has passed, then
 * leave exactly one wake-up armed. The schedule is derived state, never a chain of enqueued work,
 * so every trigger can converge on the correct state independently.
 */
class UpdateCoordinator(
    private val configRepository: ConfigRepository,
    private val cycler: WallpaperCycler,
    private val reconciler: ConfigReconciler,
    private val albumSyncer: AlbumSyncer,
    private val tickScheduler: TickScheduler,
    private val workManager: WorkManager,
) {

    companion object {
        private const val SYNC_WORK_NAME = "wallpaper_sync"
        private const val FALLBACK_WORK_NAME = "wallpaper_fallback"

        private val MIN_RETRY_DELAY = 1.minutes
        val MAX_RETRY_DELAY = 5.minutes
    }

    private val mutex = Mutex()

    /** Applies the cycle if it is due — or the recorded failed target — then re-arms. */
    suspend fun applyIfDue() = mutex.withLock { armForResult(cycler.cycleIfDue()) }

    /** Applies immediately, bypassing the interval, then re-arms from the new `lastUpdated`. */
    suspend fun applyNow(target: TargetScreen) = mutex.withLock { armForResult(cycler.cycle(target)) }

    /**
     * Applies a chosen [wallpaper] to [target], skipping the cycle's random pick. The apply is
     * recorded like a cycle apply, so the schedule re-arms from the new `lastUpdated`.
     */
    suspend fun applyWallpaper(wallpaper: Wallpaper, target: TargetScreen) = mutex.withLock {
        armForResult(cycler.applyWallpaper(wallpaper, target))
    }

    /**
     * Re-applies [wallpaper] to the screens already showing it. A wallpaper that is not current
     * anywhere changes nothing and leaves the existing tick in place.
     */
    suspend fun reapply(wallpaper: Wallpaper) = mutex.withLock { armForResult(cycler.reapply(wallpaper)) }

    /** Reconciles config after an album was deleted, then re-arms. */
    suspend fun albumsChanged() = mutex.withLock { armForResult(reconciler.reconcile() ?: WallpaperUpdateResult.NotDue) }

    /**
     * Syncs albums with the filesystem, then reconciles config and re-arms. The scan runs outside
     * the mutex so a slow SAF walk cannot block a scheduled apply.
     */
    suspend fun refresh() {
        albumSyncer.syncAlbums()
        mutex.withLock { armForResult(reconciler.reconcile() ?: WallpaperUpdateResult.NotDue) }
    }

    /** Re-arms after the interval or mirroring changed. */
    suspend fun reschedule() = mutex.withLock { armAtNextUpdate() }

    private suspend fun armForResult(result: WallpaperUpdateResult) {
        when (result) {
            WallpaperUpdateResult.Updated -> {
                this.logD { "Applied. Scheduling the next tick" }
                enqueueSync()
                armAtNextUpdate()
            }
            WallpaperUpdateResult.NotDue -> armAtNextUpdate()
            is WallpaperUpdateResult.Failed -> {
                this.logE { "Failed, retrying. cause: ${result.cause.message}" }
                enqueueSync()
                armRetry()
            }
            is WallpaperUpdateResult.Skipped -> {
                this.logW { "Skipped, waiting for the next cycle. reason: ${result.reason}" }
                armAfterInterval()
            }
        }
    }

    private suspend fun armAtNextUpdate() {
        val config = schedulableConfig() ?: return tickScheduler.cancel()
        arm(config.nextUpdate?.toEpochMilliseconds() ?: System.currentTimeMillis())
    }

    /**
     * A failure can clear on its own — a transient read, or a row the sync that just ran pruned —
     * so it retries sooner than a full interval, bounded to keep a broken album from spinning.
     */
    private suspend fun armRetry() {
        val config = schedulableConfig() ?: return tickScheduler.cancel()
        val delay = config.interval.coerceIn(MIN_RETRY_DELAY, MAX_RETRY_DELAY)
        arm(System.currentTimeMillis() + delay.inWholeMilliseconds)
    }

    /**
     * A skip leaves `lastUpdated` untouched, so `nextUpdate` is already in the past and arming it
     * would tick again immediately, forever. A skip is a condition only the user can clear (album
     * deleted, every wallpaper blacklisted), so spend the wake-up on the next full interval.
     */
    private suspend fun armAfterInterval() {
        val config = schedulableConfig() ?: return tickScheduler.cancel()
        arm(System.currentTimeMillis() + config.interval.inWholeMilliseconds)
    }

    private fun arm(triggerAtMillis: Long) {
        ensureFallbackScheduled()
        tickScheduler.armAt(triggerAtMillis)
    }

    private suspend fun schedulableConfig() =
        configRepository.configFlow.firstOrNull()?.takeIf { it.isInitialised }

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
