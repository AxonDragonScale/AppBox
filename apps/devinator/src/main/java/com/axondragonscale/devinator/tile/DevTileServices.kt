package com.axondragonscale.devinator.tile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.axondragonscale.devinator.model.DevState
import com.axondragonscale.devinator.model.WriteResult
import com.axondragonscale.devinator.repository.DevSettingsRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Created by Ronak on 11/06/26 */

/**
 * The three tiles differ only in which flag they read and write, so they share the lifecycle.
 * The tile is never the source of truth: it renders the same repository state Home does.
 */
abstract class DevTileService : TileService(), KoinComponent {

    protected val repository: DevSettingsRepository by inject()

    private var listening: Job? = null

    protected abstract fun isOn(state: DevState): Boolean

    protected abstract suspend fun write(enabled: Boolean): WriteResult

    /** The secondary label; the wireless tile replaces it with the remaining timer. */
    protected open fun subtitle(state: DevState): String = if (isOn(state)) "On" else "Off"

    override fun onStartListening() {
        super.onStartListening()
        listening?.cancel()
        listening = CoroutineScope(Dispatchers.Main.immediate).launch {
            // A tile is only bound while the shade is open, so a one-second tick is cheap and
            // keeps the wireless countdown honest.
            while (true) {
                render(repository.state.value)
                delay(1_000)
            }
        }
    }

    override fun onStopListening() {
        listening?.cancel()
        listening = null
        super.onStopListening()
    }

    override fun onDestroy() {
        listening?.cancel()
        listening = null
        super.onDestroy()
    }

    override fun onClick() {
        val next = !isOn(repository.state.value)
        CoroutineScope(Dispatchers.IO).launch {
            if (write(next) is WriteResult.Failed) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DevTileService,
                        "Couldn't change this setting.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    private fun render(state: DevState) {
        val on = isOn(state)
        qsTile?.apply {
            this.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            this.subtitle = subtitle(state)
            updateTile()
        }
    }
}

class DeveloperOptionsTileService : DevTileService() {
    override fun isOn(state: DevState): Boolean = state.developerOptions
    override suspend fun write(enabled: Boolean): WriteResult = repository.setDeveloperOptions(enabled)
}

class UsbDebuggingTileService : DevTileService() {
    override fun isOn(state: DevState): Boolean = state.usbDebugging
    override suspend fun write(enabled: Boolean): WriteResult = repository.setUsbDebugging(enabled)
}

class WirelessDebuggingTileService : DevTileService() {
    override fun isOn(state: DevState): Boolean = state.wirelessDebugging
    override suspend fun write(enabled: Boolean): WriteResult = repository.setWirelessDebugging(enabled)

    override fun subtitle(state: DevState): String {
        if (!isOn(state)) return "Off"
        val deadline = repository.autoOffDeadline.value ?: return "On"
        val seconds = (deadline - Clock.System.now()).inWholeSeconds
        if (seconds <= 0) return "On"
        return "On · ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
    }
}
