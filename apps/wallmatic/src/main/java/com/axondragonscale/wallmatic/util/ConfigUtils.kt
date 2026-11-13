package com.axondragonscale.wallmatic.util

import com.axondragonscale.wallmatic.model.Config
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.copy
import com.axondragonscale.wallmatic.model.wallpaperConfig
import kotlin.time.Duration.Companion.minutes

/**
 * Created by Ronak Harkhani on 25/09/24
 */

/** First-run setup: mirror on, 15 min cadence, both targets on [albumId]. */
fun Config.firstRun(albumId: Int): Config = copy {
    isInit = true
    mirrorHomeConfigForLock = true
    updateInterval = 15.minutes.inWholeMilliseconds
    lastUpdated = -1
    lastFailedAt = -1
    failedTarget = 0
    homeConfig = wallpaperConfig {
        this.albumId = albumId
        autoCycleEnabled = true
        currentWallpaperId = -1
    }
    lockConfig = homeConfig
}

/** Makes [albumId] the active album for [target], keeping the mirror rule in one place. */
fun Config.activateAlbum(albumId: Int, target: TargetScreen): Config = copy {
    isInit = true
    when (target) {
        TargetScreen.Home -> {
            homeConfig = homeConfig.copy {
                this.albumId = albumId
                autoCycleEnabled = true
                currentWallpaperId = -1
            }
        }
        TargetScreen.Lock -> {
            if (mirrorHomeConfigForLock) {
                homeConfig = homeConfig.copy {
                    this.albumId = albumId
                    autoCycleEnabled = true
                    currentWallpaperId = -1
                }
                lockConfig = homeConfig
            } else {
                lockConfig = lockConfig.copy {
                    this.albumId = albumId
                    autoCycleEnabled = true
                    currentWallpaperId = -1
                }
            }
        }
        TargetScreen.Both -> {
            homeConfig = homeConfig.copy {
                this.albumId = albumId
                autoCycleEnabled = true
                currentWallpaperId = -1
            }
            lockConfig = wallpaperConfig {
                this.albumId = albumId
                autoCycleEnabled = true
                currentWallpaperId = -1
            }
        }
    }
}

/** The album for [target], resolving Lock through Home when mirroring. */
fun Config.albumIdFor(target: TargetScreen): Int = when (target) {
    TargetScreen.Home -> homeConfig.albumId
    TargetScreen.Lock -> if (mirrorHomeConfigForLock) homeConfig.albumId else lockConfig.albumId
    TargetScreen.Both -> homeConfig.albumId
}

/** The targets [albumId] is explicitly active on; mirrored Lock counts as Home. */
fun Config.activeTargetsFor(albumId: Int): Set<TargetScreen> = buildSet {
    if (homeConfig.albumId == albumId) add(TargetScreen.Home)
    if (!mirrorHomeConfigForLock && lockConfig.albumId == albumId) add(TargetScreen.Lock)
}

/**
 * The target that currently shows [wallpaperId], or null when it is not applied anywhere.
 * Mirrored Lock follows Home, since it is not independently configured.
 */
fun Config.currentTargetFor(wallpaperId: Int): TargetScreen? {
    val home = homeConfig.currentWallpaperId == wallpaperId
    val lock = if (mirrorHomeConfigForLock) home else lockConfig.currentWallpaperId == wallpaperId
    return when {
        home && lock -> TargetScreen.Both
        home -> TargetScreen.Home
        lock -> TargetScreen.Lock
        else -> null
    }
}

/** Whether applying [applied] resolves the recorded failure, if any. */
fun Config.resolvesFailure(applied: TargetScreen): Boolean {
    val failed = failedTargetOrNull ?: return true
    return applied == TargetScreen.Both ||
            applied == failed ||
            (mirrorHomeConfigForLock && applied == TargetScreen.Home)
}

val Config.isAutoCycleEnabled: Boolean
    get() = homeConfig.autoCycleEnabled || lockConfig.autoCycleEnabled

val Config.nextUpdate: Long
    get() = if (isAutoCycleEnabled) lastUpdated + updateInterval else Long.MAX_VALUE

/** The target that failed last, or null when there is no unresolved failure. */
val Config.failedTargetOrNull: TargetScreen?
    get() = when {
        lastFailedAt <= 0 -> null
        failedTarget == TargetScreen.Lock.configValue -> TargetScreen.Lock
        failedTarget == TargetScreen.Both.configValue -> TargetScreen.Both
        else -> TargetScreen.Home
    }

val TargetScreen.configValue: Int
    get() = when (this) {
        TargetScreen.Home -> 0
        TargetScreen.Lock -> 1
        TargetScreen.Both -> 2
    }
