package com.axondragonscale.wallmatic.core

import android.app.WallpaperManager
import android.content.Context
import androidx.core.net.toUri
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.commons.logW
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.model.getRandomWallpaperOrNull
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
     *
     * This is the only overload that honours the update interval.
     *
     * @return [WallpaperUpdateResult.NotDue] if the interval has not elapsed,
     * [WallpaperUpdateResult.Skipped] if the config is not usable, otherwise the result of the
     * delegated update.
     */
    suspend fun updateWallpaper(): WallpaperUpdateResult {
        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update skipped. Config is null" }
            return WallpaperUpdateResult.Skipped("Config is null")
        }

        if (!config.isInit) {
            this.logD { "Wallpaper update skipped. isInit: ${config.isInit}" }
            return WallpaperUpdateResult.Skipped("Config is not initialised")
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
            return WallpaperUpdateResult.NotDue
        }

        return updateWallpaper(target)
    }

    /**
     * Updates the wallpaper for the given target, bypassing the update interval.
     * Note: If mirroring is enabled, this will update both the home and lock wallpapers.
     *
     * Config is only persisted for targets that were actually applied: `currentWallpaperId` is
     * written per applied screen, and `lastUpdated` advances as soon as one screen was applied so
     * a screen that rejects the change cannot block the cadence. A target that fails is reported
     * to the caller instead of being recorded as a success.
     *
     * A target whose album has no whitelisted wallpaper is not a failure: it is skipped and the
     * other target is still attempted. A [WallpaperUpdateResult.Failed] cause always wins over a
     * skip reason, and [WallpaperUpdateResult.Failed] is reserved for an apply that actually
     * failed.
     */
    suspend fun updateWallpaper(target: TargetScreen): WallpaperUpdateResult {
        this.logD { "Wallpaper update start. target: $target" }

        val config = appPrefsRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update skipped. Config is null" }
            return WallpaperUpdateResult.Skipped("Config is null")
        }

        if (!config.isInit) {
            this.logD { "Wallpaper update skipped. isInit: ${config.isInit}" }
            return WallpaperUpdateResult.Skipped("Config is not initialised")
        }

        val timestamp = System.currentTimeMillis()
        val homeAlbumId = config.homeConfig.albumId
        val lockAlbumId = config.lockConfig.albumId
        val homeAlbum = repository.getFullAlbum(homeAlbumId)
        val lockAlbum = if (config.mirrorHomeConfigForLock) homeAlbum
                        else repository.getFullAlbum(lockAlbumId)

        if (homeAlbum == null || lockAlbum == null) {
            this.logD { "Albums not found. home: $homeAlbumId, lock: $lockAlbumId" }
            return WallpaperUpdateResult.Skipped("Album not found. home: $homeAlbumId, lock: $lockAlbumId")
        }

        // When mirroring, always apply home wallpaper to both screens regardless of target.
        val applyHome = target.isHome() || config.mirrorHomeConfigForLock
        val applyLock = target.isLock() && !config.mirrorHomeConfigForLock

        var updatedConfig = config
        var applied = false
        var firstFailure: Throwable? = null
        var skipReason: String? = null

        if (applyHome) {
            this.logD { "Updating home wallpaper. homeAlbumId: $homeAlbumId" }
            val wallpaper = homeAlbum.getRandomWallpaperOrNull()
            if (wallpaper == null) {
                // Every wallpaper in the album is blacklisted: nothing to apply, but not a failure.
                this.logW { "Home wallpaper skipped. No whitelisted wallpaper. homeAlbumId: $homeAlbumId" }
                skipReason = skipReason ?: "No whitelisted wallpaper in album. homeAlbumId: $homeAlbumId"
            } else {
                val applyTarget = if (config.mirrorHomeConfigForLock) TargetScreen.Both else TargetScreen.Home
                setWallpaper(wallpaper, applyTarget)
                    .onSuccess {
                        updatedConfig = updatedConfig.homeConfig { currentWallpaperId = wallpaper.id }
                        if (config.mirrorHomeConfigForLock)
                            updatedConfig = updatedConfig.lockConfig { currentWallpaperId = wallpaper.id }
                        applied = true
                    }
                    .onFailure { e -> firstFailure = firstFailure ?: e }
            }
        }
        if (applyLock) {
            this.logD { "Updating lock wallpaper. lockAlbumId: $lockAlbumId" }
            val wallpaper = lockAlbum.getRandomWallpaperOrNull()
            if (wallpaper == null) {
                this.logW { "Lock wallpaper skipped. No whitelisted wallpaper. lockAlbumId: $lockAlbumId" }
                skipReason = skipReason ?: "No whitelisted wallpaper in album. lockAlbumId: $lockAlbumId"
            } else {
                setWallpaper(wallpaper, TargetScreen.Lock)
                    .onSuccess {
                        updatedConfig = updatedConfig.lockConfig { currentWallpaperId = wallpaper.id }
                        applied = true
                    }
                    .onFailure { e -> firstFailure = firstFailure ?: e }
            }
        }

        // Persist nothing unless a screen applied: the wallpaper did not change, so the cycle
        // must stay due.
        val cause = firstFailure
        if (!applied) {
            if (cause != null) {
                this.logE { "Wallpaper update failed. Nothing applied. cause: ${cause.message}" }
                return WallpaperUpdateResult.Failed(cause)
            }

            val reason = skipReason ?: "No target applied and no failure recorded. target: $target"
            this.logW { "Wallpaper update skipped. Nothing applied. reason: $reason" }
            return WallpaperUpdateResult.Skipped(reason)
        }

        appPrefsRepository.setConfig(updatedConfig.copy { lastUpdated = timestamp })
        if (cause == null) {
            this.logD { "Wallpaper update complete" }
            return WallpaperUpdateResult.Updated
        }

        this.logE { "Wallpaper update partially applied. cause: ${cause.message}" }
        return WallpaperUpdateResult.Failed(cause)
    }

    /**
     * Applies [wallpaper] to [target]. Failures are logged and returned, never thrown.
     *
     * The stream is opened eagerly and closed as soon as [WallpaperManager.setStream] returns, so a
     * missing file or a revoked URI grant is an ordinary failure instead of a leaked descriptor.
     */
    fun setWallpaper(wallpaper: Wallpaper, target: TargetScreen): Result<Unit> = runCatching {
        val stream = context.contentResolver.openInputStream(wallpaper.uri.toUri())
            ?: error("Cannot open wallpaper. uri: ${wallpaper.uri}")
        stream.use { manager.setStream(it, null, true, target.toFlag()) }
        Unit
    }.onFailure { e ->
        this.logE { "Setting wallpaper failed. wallpaperId: ${wallpaper.id}, target: $target, error: ${e.message}" }
    }

    private fun TargetScreen.toFlag() = when (this) {
        TargetScreen.Both -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        TargetScreen.Home -> WallpaperManager.FLAG_SYSTEM
        TargetScreen.Lock -> WallpaperManager.FLAG_LOCK
    }

}
