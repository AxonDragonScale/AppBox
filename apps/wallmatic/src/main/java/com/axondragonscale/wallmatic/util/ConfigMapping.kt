package com.axondragonscale.wallmatic.util

import com.axondragonscale.wallmatic.model.Interval
import com.axondragonscale.wallmatic.model.ScreenConfig
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.model.WallmaticConfig
import com.axondragonscale.wallmatic.model.WallpaperFailure
import com.axondragonscale.wallmatic.proto.Config
import com.axondragonscale.wallmatic.proto.Screen
import com.axondragonscale.wallmatic.proto.ScreenConfig as ProtoScreenConfig
import com.axondragonscale.wallmatic.proto.config
import com.axondragonscale.wallmatic.proto.failure
import com.axondragonscale.wallmatic.proto.screenConfig
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * Created by Ronak Harkhani on 11/13/26
 */

internal fun Config.toDomain(): WallmaticConfig = WallmaticConfig(
    interval = if (updateInterval > 0) updateInterval.milliseconds else Interval.default,
    lastUpdated = lastUpdated.takeIf { it > 0 }?.let(Instant::fromEpochMilliseconds),
    mirrorHomeForLock = mirrorHomeForLock,
    home = home.toDomain(),
    lock = lock.toDomain(),
    failure = if (hasFailure()) {
        WallpaperFailure(failure.target.toTargetScreen(), Instant.fromEpochMilliseconds(failure.at))
    } else {
        null
    },
)

internal fun WallmaticConfig.toProto(): Config = config {
    updateInterval = interval.inWholeMilliseconds
    lastUpdated = this@toProto.lastUpdated?.toEpochMilliseconds() ?: 0
    mirrorHomeForLock = this@toProto.mirrorHomeForLock
    home = this@toProto.home.toProto()
    lock = this@toProto.lock.toProto()
    this@toProto.failure?.let { recorded ->
        failure = failure {
            target = recorded.target.toScreen()
            at = recorded.at.toEpochMilliseconds()
        }
    }
}

private fun ScreenConfig.toProto() = screenConfig {
    albumId = this@toProto.albumId ?: 0
    currentWallpaperId = this@toProto.currentWallpaperId ?: 0
}

private fun ProtoScreenConfig.toDomain() = ScreenConfig(
    albumId = albumId.takeIf { it > 0 },
    currentWallpaperId = currentWallpaperId.takeIf { it > 0 },
)

private fun Screen.toTargetScreen(): TargetScreen = when (this) {
    Screen.SCREEN_LOCK -> TargetScreen.Lock
    Screen.SCREEN_BOTH -> TargetScreen.Both
    else -> TargetScreen.Home
}

private fun TargetScreen.toScreen(): Screen = when (this) {
    TargetScreen.Home -> Screen.SCREEN_HOME
    TargetScreen.Lock -> Screen.SCREEN_LOCK
    TargetScreen.Both -> Screen.SCREEN_BOTH
}
