package com.axondragonscale.wallmatic.core

import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.WallpaperUpdateResult
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/**
 * Reconciles config with the albums that still exist: a missing or empty album is cleared and a
 * stale current wallpaper is re-applied. Only reads Room and config, never scans SAF folders, so it
 * is the cheap pass to run after an album is deleted.
 */
class ConfigReconciler(
    private val configRepository: ConfigRepository,
    private val repository: WallmaticRepository,
    private val cycler: WallpaperCycler,
) {

    /** Reconciles the configured albums; returns the re-apply, preferring a failure. */
    suspend fun reconcile(): WallpaperUpdateResult? {
        val config = configRepository.configFlow.firstOrNull() ?: return null
        if (!config.isInitialised) return null

        var last: WallpaperUpdateResult? = null

        config.home.albumId?.let { albumId ->
            val result = reconcileScreen(TargetScreen.Home, albumId, config.home.currentWallpaperId)
            last = preferFailure(last, result)
        }
        if (!config.mirrorHomeForLock) {
            config.lock.albumId?.let { albumId ->
                val result = reconcileScreen(TargetScreen.Lock, albumId, config.lock.currentWallpaperId)
                last = preferFailure(last, result)
            }
        }

        return last
    }

    private suspend fun reconcileScreen(
        target: TargetScreen,
        albumId: Int,
        currentId: Int?,
    ): WallpaperUpdateResult? {
        val album = repository.getFullAlbum(albumId)
        if (album == null || !album.hasWallpapers) {
            configRepository.update { if (target == TargetScreen.Home) it.clearHome() else it.clearLock() }
            return null
        }
        if (album.allWallpapers.none { it.id == currentId }) {
            val result = cycler.cycle(target)
            this.logD { "Config reconcile wallpaper update. target: $target, result: $result" }
            return result
        }
        return null
    }

    /** A failure must win so `albumsChanged()` arms the bounded retry, not the next interval. */
    private fun preferFailure(
        current: WallpaperUpdateResult?,
        next: WallpaperUpdateResult?,
    ): WallpaperUpdateResult? = when {
        next is WallpaperUpdateResult.Failed -> next
        current is WallpaperUpdateResult.Failed -> current
        else -> next ?: current
    }

}
