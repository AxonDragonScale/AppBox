package com.axondragonscale.wallmatic.core

import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.commons.logW
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.WallmaticConfig
import com.axondragonscale.wallmatic.model.WallpaperFailure
import com.axondragonscale.wallmatic.model.WallpaperUpdateResult
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/**
 * Picks and persists wallpaper changes. The Android side of an apply lives in [WallpaperApplier];
 * this class owns config, album selection and the cadence rule.
 */
class WallpaperCycler(
    private val configRepository: ConfigRepository,
    private val repository: WallmaticRepository,
    private val applier: WallpaperApplier,
) {

    /**
     * Applies the cycle if it is due, or retries the recorded failed target, then returns the
     * outcome. This is the only path that honours the interval.
     */
    suspend fun cycleIfDue(): WallpaperUpdateResult {
        val config = configRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update skipped. Config is null" }
            return WallpaperUpdateResult.Skipped("Config is null")
        }

        if (!config.isInitialised) {
            this.logD { "Wallpaper update skipped. isInitialised: false" }
            return WallpaperUpdateResult.Skipped("Config is not initialised")
        }

        config.failure?.let { failure ->
            this.logD { "Wallpaper update retrying failed target: ${failure.target}" }
            return cycle(failure.target)
        }

        val nextUpdate = config.nextUpdate
        if (nextUpdate != null && nextUpdate > Clock.System.now()) {
            this.logD { "Wallpaper update skipped. Next update not due yet." }
            return WallpaperUpdateResult.NotDue
        }

        val homeConfigured = config.home.albumId != null
        val lockConfigured = config.lock.albumId != null
        val mirror = config.mirrorHomeForLock && homeConfigured
        val target = when {
            homeConfigured && (lockConfigured || mirror) -> TargetScreen.Both
            homeConfigured -> TargetScreen.Home
            lockConfigured -> TargetScreen.Lock
            else -> null
        }
        if (target == null) {
            this.logD { "Wallpaper update skipped. No album configured." }
            return WallpaperUpdateResult.Skipped("No album configured")
        }

        return cycle(target)
    }

    /**
     * Applies a random wallpaper for [target], bypassing the interval.
     *
     * Config is only persisted for screens that actually applied: `currentWallpaperId` is written
     * per applied screen, and `lastUpdated` advances as soon as one screen applied so a screen that
     * rejects the change cannot block the cadence.
     *
     * A screen whose album has no whitelisted wallpaper is not a failure: it is skipped and the
     * other screen is still attempted. [WallpaperUpdateResult.Failed] is reserved for an apply that
     * actually failed and always wins over a skip reason.
     */
    suspend fun cycle(target: TargetScreen): WallpaperUpdateResult {
        this.logD { "Wallpaper update start. target: $target" }

        val config = configRepository.configFlow.firstOrNull() ?: run {
            this.logD { "Wallpaper update skipped. Config is null" }
            return WallpaperUpdateResult.Skipped("Config is null")
        }

        if (!config.isInitialised) {
            this.logD { "Wallpaper update skipped. isInitialised: false" }
            return WallpaperUpdateResult.Skipped("Config is not initialised")
        }

        val now = Clock.System.now()

        val homeConfigured = config.home.albumId != null
        val lockConfigured = config.lock.albumId != null
        // Without a Home album there is nothing to mirror from.
        val mirror = config.mirrorHomeForLock && homeConfigured

        val applyHome = homeConfigured && (target.isHome() || mirror)
        val applyLock = lockConfigured && target.isLock() && !mirror

        val homeAlbumId = config.home.albumId
        val lockAlbumId = config.albumIdFor(TargetScreen.Lock)
        val homeAlbum = homeAlbumId?.takeIf { applyHome }?.let { repository.getFullAlbum(it) }
        val lockAlbum = lockAlbumId?.takeIf { applyLock }?.let { repository.getFullAlbum(it) }

        if ((applyHome && homeAlbum == null) || (applyLock && lockAlbum == null)) {
            this.logD { "Albums not found. home: $homeAlbumId, lock: $lockAlbumId" }
            return WallpaperUpdateResult.Skipped("Album not found. home: $homeAlbumId, lock: $lockAlbumId")
        }

        var homeWallpaperId: Int? = null
        var lockWallpaperId: Int? = null
        var applied = false
        var firstFailure: Throwable? = null
        var firstFailedTarget: TargetScreen? = null
        var skipReason: String? = null

        if (applyHome) {
            this.logD { "Updating home wallpaper. homeAlbumId: $homeAlbumId" }
            val wallpaper = homeAlbum?.getRandomWallpaperOrNull()
            if (wallpaper == null) {
                // Every wallpaper in the album is blacklisted: nothing to apply, but not a failure.
                this.logW { "Home wallpaper skipped. No whitelisted wallpaper. homeAlbumId: $homeAlbumId" }
                skipReason = skipReason ?: "No whitelisted wallpaper in album. homeAlbumId: $homeAlbumId"
            } else {
                val applyTarget = effectiveTarget(config, TargetScreen.Home)
                applier.apply(wallpaper, applyTarget)
                    .onSuccess {
                        homeWallpaperId = wallpaper.id
                        if (mirror) lockWallpaperId = wallpaper.id
                        applied = true
                    }
                    .onFailure { e ->
                        firstFailure = firstFailure ?: e
                        firstFailedTarget = firstFailedTarget ?: applyTarget
                    }
            }
        }
        if (applyLock) {
            this.logD { "Updating lock wallpaper. lockAlbumId: $lockAlbumId" }
            val wallpaper = lockAlbum?.getRandomWallpaperOrNull()
            if (wallpaper == null) {
                this.logW { "Lock wallpaper skipped. No whitelisted wallpaper. lockAlbumId: $lockAlbumId" }
                skipReason = skipReason ?: "No whitelisted wallpaper in album. lockAlbumId: $lockAlbumId"
            } else {
                applier.apply(wallpaper, TargetScreen.Lock)
                    .onSuccess {
                        lockWallpaperId = wallpaper.id
                        applied = true
                    }
                    .onFailure { e ->
                        firstFailure = firstFailure ?: e
                        firstFailedTarget = firstFailedTarget ?: TargetScreen.Lock
                    }
            }
        }

        val cause = firstFailure
        if (!applied) {
            if (cause != null) {
                this.logE { "Wallpaper update failed. Nothing applied. cause: ${cause.message}" }
                // Nothing applied, so the cadence must not advance: record the failure only and let
                // the coordinator retry the recorded target.
                configRepository.update { current ->
                    current.recordFailure(firstFailedTarget ?: target, now)
                }
                return WallpaperUpdateResult.Failed(cause)
            }

            val reason = skipReason ?: "No target applied and no failure recorded. target: $target"
            this.logW { "Wallpaper update skipped. Nothing applied. reason: $reason" }
            return WallpaperUpdateResult.Skipped(reason)
        }

        // Record exactly the screens that applied, so a lock-only config writes the Lock id.
        val recordedTarget = when {
            homeWallpaperId != null && lockWallpaperId != null -> TargetScreen.Both
            homeWallpaperId != null -> TargetScreen.Home
            lockWallpaperId != null -> TargetScreen.Lock
            else -> target
        }
        configRepository.update { current ->
            current.recordApplied(
                target = recordedTarget,
                homeWallpaperId = homeWallpaperId,
                lockWallpaperId = lockWallpaperId,
                at = now,
                failure = cause?.let { WallpaperFailure(firstFailedTarget ?: target, now) },
            )
        }
        if (cause == null) {
            this.logD { "Wallpaper update complete" }
            return WallpaperUpdateResult.Updated
        }

        this.logE { "Wallpaper update partially applied. cause: ${cause.message}" }
        return WallpaperUpdateResult.Failed(cause)
    }

    /**
     * Applies [wallpaper] to [target] and records it as current, exactly as a cycle apply does.
     * Only the pick is bypassed, so the next tick stays a full interval away.
     *
     * While mirroring, Home and Lock are one wallpaper, so any request is applied and recorded as
     * Both; otherwise a request for one screen would leave the other on an older wallpaper.
     */
    suspend fun applyWallpaper(wallpaper: Wallpaper, target: TargetScreen): WallpaperUpdateResult {
        val now = Clock.System.now()
        val config = configRepository.configFlow.firstOrNull()
            ?: return WallpaperUpdateResult.Skipped("Config is null")
        val appliedTarget = effectiveTarget(config, target)

        val cause = applier.apply(wallpaper, appliedTarget).exceptionOrNull()
        if (cause != null) {
            this.logE { "Wallpaper apply failed. wallpaperId: ${wallpaper.id}, cause: ${cause.message}" }
            return WallpaperUpdateResult.Failed(cause)
        }

        configRepository.update { current ->
            current.recordApplied(
                target = appliedTarget,
                homeWallpaperId = wallpaper.id.takeIf { appliedTarget.isHome() },
                lockWallpaperId = wallpaper.id.takeIf { appliedTarget.isLock() },
                at = now,
            )
        }
        this.logD { "Wallpaper applied. wallpaperId: ${wallpaper.id}, target: $appliedTarget" }
        return WallpaperUpdateResult.Updated
    }

    /** Re-applies [wallpaper] to the screens already showing it; not current anywhere -> NotDue. */
    suspend fun reapply(wallpaper: Wallpaper): WallpaperUpdateResult {
        val config = configRepository.configFlow.firstOrNull() ?: return WallpaperUpdateResult.NotDue
        val target = config.currentTargetFor(wallpaper.id) ?: return WallpaperUpdateResult.NotDue
        return applyWallpaper(wallpaper, target)
    }

    /** Mirroring makes Home and Lock one target; every apply and record goes through this. */
    private fun effectiveTarget(config: WallmaticConfig, target: TargetScreen): TargetScreen =
        if (config.mirrorHomeForLock && config.home.albumId != null) TargetScreen.Both else target

}
