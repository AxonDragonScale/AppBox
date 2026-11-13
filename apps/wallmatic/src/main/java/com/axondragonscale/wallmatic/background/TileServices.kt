package com.axondragonscale.wallmatic.background

import android.service.quicksettings.TileService
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.model.TargetScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 11/13/26
 */

/** A tile tap applies immediately, bypassing the interval, then re-arms from the new apply. */
abstract class TargetTileService : TileService(), KoinComponent {

    protected abstract val target: TargetScreen

    private val updateCoordinator: UpdateCoordinator by inject()

    override fun onClick() {
        this.logD { "onClick: $target" }
        CoroutineScope(Dispatchers.IO).launch { updateCoordinator.applyNow(target) }
    }

}

class WallmaticTileService : TargetTileService() {
    override val target = TargetScreen.Both
}

class HomeTileService : TargetTileService() {
    override val target = TargetScreen.Home
}

class LockTileService : TargetTileService() {
    override val target = TargetScreen.Lock
}
