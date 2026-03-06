package com.axondragonscale.wallmatic.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Primary scheduled wallpaper update worker.
 *
 * Acts as the coordinator for the update cycle:
 *   1. Syncs albums with the filesystem
 *   2. Updates the wallpaper based on config
 *   3. Schedules the next update
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

    override suspend fun doWork(): Result {
        this.logD { "doWork start" }
        return runCatching {
            syncManager.syncAlbums()
            wallpaperUpdater.updateWallpaper()
            scheduler.scheduleNextUpdate()
            this.logD { "doWork complete" }
            Result.success()
        }.getOrElse { e ->
            this.logD { "doWork failed: $e" }
            Result.retry()
        }
    }

}
