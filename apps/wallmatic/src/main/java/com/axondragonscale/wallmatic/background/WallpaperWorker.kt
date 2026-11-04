package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.axondragonscale.commons.log
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.commons.logW
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.core.WallpaperUpdateResult
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Primary scheduled wallpaper update worker.
 *
 * Acts as the coordinator for the update cycle:
 *   1. Syncs albums with the filesystem
 *   2. Updates the wallpaper based on config
 *   3. Appends the next update to the chain
 *
 * The next update is scheduled here (not inside WallpaperUpdater) because
 * scheduling is the caller's responsibility, not the updater's.
 */
class WallpaperWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val syncManager: SyncManager by inject()
    private val wallpaperUpdater: WallpaperUpdater by inject()
    private val scheduler: WallpaperWorkScheduler by inject()

    /**
     * Runs one update cycle.
     *
     * Invariant: every non-cancelled run appends exactly one successor —
     * [WallpaperWorkScheduler.appendNextUpdate] after the cycle completed (`Updated` / `NotDue`),
     * or [WallpaperWorkScheduler.appendRetry] after a cycle that applied nothing (`Failed` /
     * `Skipped`), whose stored due time is therefore still in the past. The run always reports
     * [Result.success] because the successor already covers the recovery; a retrying work
     * instance would compete with it under the same unique work name.
     *
     * Appending (not replacing) is what makes this safe: the successor becomes a dependent of this
     * work, so scheduling it from inside `doWork()` never cancels the run that is executing this
     * code. `ExistingWorkPolicy.REPLACE` here would cancel the running work.
     */
    override suspend fun doWork(): Result {
        this.logD { "doWork start" }
        try {
            syncManager.syncAlbums()

            when (val result = wallpaperUpdater.updateWallpaper()) {
                is WallpaperUpdateResult.Failed -> {
                    this.logE { "doWork failed. wallpaper update error: ${result.cause.message}" }
                    scheduler.appendRetry()
                }
                is WallpaperUpdateResult.Skipped -> {
                    this.logW { "doWork skipped, scheduling a retry. reason: ${result.reason}" }
                    scheduler.appendRetry()
                }
                WallpaperUpdateResult.Updated, WallpaperUpdateResult.NotDue -> {
                    this.logD { "Wallpaper update result: $result" }
                    scheduler.appendNextUpdate()
                }
            }

            this.logD { "doWork complete" }
            return Result.success()
        } catch (e: CancellationException) {
            // Cancelled work must not schedule a successor or report an outcome.
            // WorkManager/the watchdog revives the chain.
            this.logD { "doWork cancelled" }
            throw e
        } catch (e: Exception) {
            this.logE { "doWork failed, scheduling a retry. error: ${e.message}" }
            this.log { e }
            scheduler.appendRetry()
            return Result.success()
        }
    }

}
