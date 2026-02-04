package com.axondragonscale.wallmatic.util

import com.axondragonscale.wallmatic.model.Config
import com.axondragonscale.wallmatic.model.ConfigKt
import com.axondragonscale.wallmatic.model.WallpaperConfig
import com.axondragonscale.wallmatic.model.WallpaperConfigKt
import com.axondragonscale.wallmatic.model.copy

/**
 * Created by Ronak Harkhani on 25/09/24
 */

fun Config.mirrorHomeConfigForLock(block: ConfigKt.Dsl.() -> Boolean) = this.copy {
    mirrorHomeConfigForLock = block()
}

fun Config.homeConfig(block: WallpaperConfigKt.Dsl.() -> Unit) = this.copy {
    homeConfig = homeConfig.copy { block() }
    if (mirrorHomeConfigForLock) lockConfig = homeConfig
}

fun Config.lockConfig(block: WallpaperConfigKt.Dsl.() -> Unit) = this.copy {
    lockConfig = lockConfig.copy { block() }
}

val WallpaperConfig.nextUpdate: Long
    get() = if (autoCycleEnabled) lastUpdated + updateInterval else Long.MAX_VALUE
