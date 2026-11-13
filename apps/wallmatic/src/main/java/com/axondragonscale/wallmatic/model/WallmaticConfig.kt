package com.axondragonscale.wallmatic.model

import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 11/13/26
 */

data class ScreenConfig(
    val albumId: Int? = null,
    val currentWallpaperId: Int? = null,
)

data class WallpaperFailure(
    val target: TargetScreen,
    val at: Instant,
)

data class WallmaticConfig(
    val interval: Duration = Interval.default,
    val lastUpdated: Instant? = null,
    val mirrorHomeForLock: Boolean = true,
    val home: ScreenConfig = ScreenConfig(),
    val lock: ScreenConfig = ScreenConfig(),
    val failure: WallpaperFailure? = null,
) {
    val isInitialised: Boolean get() = home.albumId != null || (!mirrorHomeForLock && lock.albumId != null)
    val nextUpdate: Instant? get() = lastUpdated?.plus(interval)

    fun albumIdFor(target: TargetScreen): Int? = when (target) {
        TargetScreen.Home -> home.albumId
        TargetScreen.Lock -> if (mirrorHomeForLock) home.albumId else lock.albumId
        TargetScreen.Both -> home.albumId ?: lock.albumId
    }

    fun currentWallpaperIdFor(target: TargetScreen): Int? = when (target) {
        TargetScreen.Home -> home.currentWallpaperId
        TargetScreen.Lock -> if (mirrorHomeForLock) home.currentWallpaperId else lock.currentWallpaperId
        TargetScreen.Both -> home.currentWallpaperId ?: lock.currentWallpaperId
    }

    /** Screens already showing [wallpaperId]; a mirrored Lock follows Home. */
    fun currentTargetFor(wallpaperId: Int): TargetScreen? {
        val homeCurrent = home.currentWallpaperId == wallpaperId
        val lockCurrent = if (mirrorHomeForLock) homeCurrent else lock.currentWallpaperId == wallpaperId
        return when {
            homeCurrent && lockCurrent -> TargetScreen.Both
            homeCurrent -> TargetScreen.Home
            lockCurrent -> TargetScreen.Lock
            else -> null
        }
    }

    /** The screens [albumId] is explicitly configured on; mirrored Lock counts as Home. */
    fun activeTargetsFor(albumId: Int): Set<TargetScreen> = buildSet {
        if (home.albumId == albumId) add(TargetScreen.Home)
        if (!mirrorHomeForLock && lock.albumId == albumId) add(TargetScreen.Lock)
    }

    fun activeLabelFor(albumId: Int): String? {
        val targets = activeTargetsFor(albumId)
        if (targets.isEmpty()) return null
        val onHome = TargetScreen.Home in targets
        val onLock = TargetScreen.Lock in targets
        return when {
            onHome && onLock -> TargetScreen.Both.label
            mirrorHomeForLock && onHome -> TargetScreen.Both.label
            onHome -> TargetScreen.Home.label
            else -> TargetScreen.Lock.label
        }
    }

    /** Menu entries that would change [albumId]'s activation. */
    fun activatableTargetsFor(albumId: Int): List<TargetScreen> =
        if (mirrorHomeForLock) {
            if (home.albumId == albumId) emptyList() else listOf(TargetScreen.Both)
        } else {
            buildList {
                if (home.albumId != albumId) add(TargetScreen.Home)
                if (lock.albumId != albumId) add(TargetScreen.Lock)
            }
        }

    fun resolvesFailure(applied: TargetScreen): Boolean {
        val failed = failure?.target ?: return true
        return applied == TargetScreen.Both ||
            applied == failed ||
            (mirrorHomeForLock && applied == TargetScreen.Home)
    }

    fun activate(albumId: Int, target: TargetScreen): WallmaticConfig {
        val screen = ScreenConfig(albumId = albumId)
        return when (target) {
            TargetScreen.Home -> copy(home = screen, failure = null)
            TargetScreen.Lock ->
                if (mirrorHomeForLock) copy(home = screen, failure = null)
                else copy(lock = screen, failure = null)
            TargetScreen.Both -> copy(home = screen, lock = screen, failure = null)
        }
    }

    fun firstRun(albumId: Int): WallmaticConfig = WallmaticConfig(
        interval = Interval.default,
        mirrorHomeForLock = true,
        home = ScreenConfig(albumId = albumId),
        lock = ScreenConfig(albumId = albumId),
    )

    /** Forgets Home's album and any recorded failure. */
    fun clearHome(): WallmaticConfig = copy(home = ScreenConfig(), failure = null)

    /** Forgets Lock's album and any recorded failure; Home is untouched. */
    fun clearLock(): WallmaticConfig = copy(lock = ScreenConfig(), failure = null)

    /** Records a failed apply without advancing [lastUpdated], so the cycle stays due. */
    fun recordFailure(target: TargetScreen, at: Instant): WallmaticConfig =
        copy(failure = WallpaperFailure(target, at))

    /**
     * Records a successful apply. [target] is what was attempted, not necessarily what succeeded.
     * A null [failure] clears the recorded failure only when [target] resolves it.
     */
    fun recordApplied(
        target: TargetScreen,
        homeWallpaperId: Int?,
        lockWallpaperId: Int?,
        at: Instant,
        failure: WallpaperFailure? = null,
    ): WallmaticConfig {
        var next = copy(lastUpdated = at)
        if (target.isHome() && homeWallpaperId != null) {
            next = next.copy(home = home.copy(currentWallpaperId = homeWallpaperId))
        }
        if (target.isLock() && lockWallpaperId != null) {
            next = next.copy(lock = lock.copy(currentWallpaperId = lockWallpaperId))
        }
        return when {
            failure != null -> next.copy(failure = failure)
            resolvesFailure(target) -> next.copy(failure = null)
            else -> next
        }
    }
}
