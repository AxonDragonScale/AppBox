package com.axondragonscale.wallmatic.background

import android.service.quicksettings.TileService
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import com.axondragonscale.wallmatic.model.TargetScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 22/09/24
 */
class LockTileService: TileService(), KoinComponent {

    private val wallpaperUpdater: WallpaperUpdater by inject()

    override fun onClick() {
        this.logD { "onClick" }
        CoroutineScope(Dispatchers.IO).launch {
            wallpaperUpdater.updateWallpaper(TargetScreen.Lock)
        }
    }

}
