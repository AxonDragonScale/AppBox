package com.axondragonscale.wallmatic.core

import android.app.WallpaperManager
import android.content.Context
import androidx.core.net.toUri
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.model.getRandomWallpaper
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.util.homeConfig
import com.axondragonscale.wallmatic.util.lockConfig
import com.axondragonscale.wallmatic.util.nextUpdate
import kotlinx.coroutines.flow.firstOrNull

/**
 * Created by Ronak Harkhani on 23/09/24
 */
class WallpaperUpdater(
    val context: Context,
    private val appPrefsRepository: AppPrefsRepository,
    private val repository: WallmaticRepository,
) {

    private val manager = WallpaperManager.getInstance(context)

    /**
     * Update one or both wallpapers if the updateInterval has passed since the last update.
     * Determines target screen based on config and delegates to the TargetScreen overload.
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
        val updateHome = config.homeConfig.autoCycleEnabled && config.nextUpdate < now
        val updateLock = config.lockConfig.autoCycleEnabled && config.nextUpdate < now

        val target = when {
            updateHome && (updateLock || config.mirrorHomeConfigForLock) -> TargetScreen.Both
            updateHome -> TargetScreen.Home
            updateLock -> TargetScreen.Lock
            else -> null
        } ?: run {
            this.logD { "Wallpaper update skipped. Next update not due yet." }
            return
        }

        updateWallpaper(target)
    }

    /**
     * Updates the wallpaper for the given target.
     * Note: If mirroring is enabled, this will update both the home and lock wallpapers.
     */
    suspend fun updateWallpaper(target: TargetScreen) {
        this.logD { "Wallpaper update start. target: $target" }

        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update failed. Config is null" }
            return
        }

        if (!config.isInit) {
            this.logD { "Wallpaper update skipped. isInit: ${config.isInit}" }
            return
        }

        val timestamp = System.currentTimeMillis()
        val homeAlbumId = config.homeConfig.albumId
        val lockAlbumId = config.lockConfig.albumId
        val homeAlbum = repository.getFullAlbum(homeAlbumId)
        val lockAlbum = if (config.mirrorHomeConfigForLock) homeAlbum
                        else repository.getFullAlbum(lockAlbumId)

        if (homeAlbum == null || lockAlbum == null) {
            this.logD { "Albums not found. home: $homeAlbumId, lock: $lockAlbumId" }
            return
        }

        // When mirroring, always apply home wallpaper to both screens regardless of target.
        val applyHome = target.isHome() || config.mirrorHomeConfigForLock
        val applyLock = target.isLock() && !config.mirrorHomeConfigForLock

        var updatedConfig = config
        if (applyHome) {
            this.logD { "Updating home wallpaper. homeAlbumId: $homeAlbumId" }
            val wallpaper = homeAlbum.getRandomWallpaper()
            val applyTarget = if (config.mirrorHomeConfigForLock) TargetScreen.Both else TargetScreen.Home
            setWallpaper(wallpaper, applyTarget)
            updatedConfig = updatedConfig.homeConfig { currentWallpaperId = wallpaper.id }
            if (config.mirrorHomeConfigForLock)
                updatedConfig = updatedConfig.lockConfig { currentWallpaperId = wallpaper.id }
        }
        if (applyLock) {
            this.logD { "Updating lock wallpaper. lockAlbumId: $lockAlbumId" }
            val wallpaper = lockAlbum.getRandomWallpaper()
            setWallpaper(wallpaper, TargetScreen.Lock)
            updatedConfig = updatedConfig.lockConfig { currentWallpaperId = wallpaper.id }
        }
        appPrefsRepository.setConfig(updatedConfig.copy { lastUpdated = timestamp })
        this.logD { "Wallpaper update complete" }
    }

    fun setWallpaper(wallpaper: Wallpaper, target: TargetScreen) = runCatching {
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
