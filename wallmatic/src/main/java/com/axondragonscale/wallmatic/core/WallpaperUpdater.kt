package com.axondragonscale.wallmatic.core

import android.app.WallpaperManager
import android.content.Context
import androidx.core.net.toUri
import com.axondragonscale.wallmatic.background.WallmaticScheduler
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.getWhitelistedWallpapers
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.homeConfig
import com.axondragonscale.wallmatic.util.lockConfig
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlinx.coroutines.flow.firstOrNull

/**
 * Created by Ronak Harkhani on 23/09/24
 */
class WallpaperUpdater(
    val context: Context,
    private val appPrefsRepository: AppPrefsRepository,
    private val repository: WallmaticRepository,
    private val scheduler: WallmaticScheduler,
) {

    private val manager = WallpaperManager.getInstance(context)

    /**
     * Update one or both wallpapers if the updateInterval has passed since the last update.
     * Determines if one or both wallpapers should be update based on config.
     * Schedules the next update if wallpapers are not updated.
     */
    suspend fun updateWallpaper() {
        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update failed. Config is null" }
            return
        }

        if (!config.isInit) {
            this.logD { "Wallpaper update skipped. isInit: ${config.isInit}" }
            return
        }

        val now = System.currentTimeMillis()
        val updateHomeWallpaper = config.homeConfig.nextUpdate < now
        val updateLockWallpaper = config.lockConfig.nextUpdate < now

        val target = when {
            updateHomeWallpaper && updateLockWallpaper -> TargetScreen.Both
            updateHomeWallpaper && config.mirrorHomeConfigForLock -> TargetScreen.Both
            updateHomeWallpaper -> TargetScreen.Home
            updateLockWallpaper -> TargetScreen.Lock
            else -> null
        }

        if (target != null) updateWallpaper(target)
        else scheduler.scheduleNextUpdate()
    }

    /**
     * Updates the wallpaper for the given target.
     * Note: If mirroring is enabled, this will update both the home and lock wallpapers.
     */
    suspend fun updateWallpaper(target: TargetScreen) {
        this.logD { "Wallpaper update start. target: $target" }

        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logE("Wallpaper update failed. Config is null")
            return
        }

        if (!config.isInit) {
            this.logE("Wallpaper update skipped. isInit: ${config.isInit}")
            return
        }

        val timestamp = System.currentTimeMillis()
        val homeAlbumId = config.homeConfig.albumId
        val lockAlbumId = config.lockConfig.albumId
        val homeAlbum = repository.getFullAlbum(homeAlbumId)
        val lockAlbum = if (config.mirrorHomeConfigForLock) homeAlbum
                        else repository.getFullAlbum(lockAlbumId)

        if (homeAlbum == null || lockAlbum == null) {
            this.logE("Albums not found. home: $homeAlbumId, lock: $lockAlbumId")
            return
        }

        if (config.mirrorHomeConfigForLock) {
            this.logD { "Updating wallpaper. homeAlbumId: $homeAlbumId, lockAlbumId: $lockAlbumId" }
            val wallpaper = homeAlbum.getWhitelistedWallpapers().random()
            setWallpaper(wallpaper, TargetScreen.Both)
            // Lock Config automatically updated since mirroring is enabled
            appPrefsRepository.setConfig(config.homeConfig {
                currentWallpaperId = wallpaper.id
                lastUpdated = timestamp
            })
        } else {
            if (target.isHome()) {
                this.logD { "Updating home wallpaper. homeAlbumId: $homeAlbumId" }
                val wallpaper = homeAlbum.getWhitelistedWallpapers().random()
                setWallpaper(wallpaper, TargetScreen.Home)
                appPrefsRepository.setConfig(config.homeConfig {
                    currentWallpaperId = wallpaper.id
                    lastUpdated = timestamp
                })
            }
            if (target.isLock()) {
                this.logD { "Updating lock wallpaper. lockAlbumId: $lockAlbumId" }
                val wallpaper = lockAlbum.getWhitelistedWallpapers().random()
                setWallpaper(wallpaper, TargetScreen.Lock)
                appPrefsRepository.setConfig(config.lockConfig {
                    currentWallpaperId = wallpaper.id
                    lastUpdated = timestamp
                })
            }
        }
        this.logD { "Wallpaper update complete" }

        scheduler.scheduleNextUpdate()
    }

    private fun setWallpaper(wallpaper: Wallpaper, target: TargetScreen) {
        manager.setStream(
            context.contentResolver.openInputStream(wallpaper.uri.toUri()),
            null,
            true,
            target.toFlag(),
        )
    }

    private fun TargetScreen.toFlag() = when (this) {
        TargetScreen.Both -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        TargetScreen.Home -> WallpaperManager.FLAG_SYSTEM
        TargetScreen.Lock -> WallpaperManager.FLAG_LOCK
    }

}
