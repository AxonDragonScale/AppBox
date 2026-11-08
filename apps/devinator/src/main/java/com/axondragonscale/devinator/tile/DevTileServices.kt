package com.axondragonscale.devinator.tile

import android.service.quicksettings.Tile
import com.axondragonscale.devinator.model.DevState
import com.axondragonscale.devinator.model.WRITE_FAILED_MESSAGE
import com.axondragonscale.devinator.model.WriteResult
import com.axondragonscale.devinator.model.hasTimeLeft
import com.axondragonscale.devinator.model.remainingText
import com.axondragonscale.devinator.permission.GrantRepository
import com.axondragonscale.devinator.repository.DevSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/** Created by Ronak on 11/06/26 */

/**
 * The three flag tiles differ only in which flag they read and write, so they share the lifecycle.
 * The tile is never the source of truth: it renders the same repository state Home does.
 */
abstract class DevTileService : ListeningTileService() {

    protected val repository: DevSettingsRepository by inject()
    private val grant: GrantRepository by inject()

    protected abstract fun isOn(state: DevState): Boolean

    protected abstract suspend fun write(enabled: Boolean): WriteResult

    /** USB and wireless are ineffective while the master switch is off. */
    protected open val requiresDeveloperOptions: Boolean get() = false

    /** The secondary label; the wireless tile replaces it with the remaining timer. */
    protected open fun subtitle(state: DevState): String = if (isOn(state)) "On" else "Off"

    override fun onClick() {
        CoroutineScope(Dispatchers.IO).launch {
            if (requiresDeveloperOptions && !repository.isDeveloperOptionsOn()) return@launch
            val next = !isOn(repository.state.value)
            when (write(next)) {
                WriteResult.Failed -> toast(WRITE_FAILED_MESSAGE)
                WriteResult.AwaitingConsent -> toast("Confirm the system dialog")
                WriteResult.Ok -> Unit
            }
        }
    }

    override fun render() {
        val tile = qsTile ?: return
        if (!grant.isGranted()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.subtitle = GRANT_ACCESS_LABEL
            tile.updateTile()
            return
        }

        val state = repository.state.value
        if (requiresDeveloperOptions && !state.developerOptions) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.subtitle = DEV_OPTIONS_OFF_LABEL
            tile.updateTile()
            return
        }

        val on = isOn(state)
        tile.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = subtitle(state)
        tile.updateTile()
    }

    override suspend fun reconcile() = repository.reconcileDeadline()
}

class DeveloperOptionsTileService : DevTileService() {
    override fun isOn(state: DevState): Boolean = state.developerOptions
    override suspend fun write(enabled: Boolean): WriteResult = repository.setDeveloperOptions(enabled)
}

class UsbDebuggingTileService : DevTileService() {
    override val requiresDeveloperOptions: Boolean get() = true
    override fun isOn(state: DevState): Boolean = state.usbDebugging
    override suspend fun write(enabled: Boolean): WriteResult = repository.setUsbDebugging(enabled)
}

class WirelessDebuggingTileService : DevTileService() {
    override val requiresDeveloperOptions: Boolean get() = true
    override fun isOn(state: DevState): Boolean = state.wirelessDebugging
    override suspend fun write(enabled: Boolean): WriteResult = repository.setWirelessDebugging(enabled)

    override fun subtitle(state: DevState): String {
        if (!isOn(state)) return "Off"
        val deadline = repository.autoOffDeadline.value
        return if (deadline.hasTimeLeft()) "On · ${deadline.remainingText()}" else "On"
    }
}
