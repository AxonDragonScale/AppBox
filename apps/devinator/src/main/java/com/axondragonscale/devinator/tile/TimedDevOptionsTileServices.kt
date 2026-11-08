package com.axondragonscale.devinator.tile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.axondragonscale.devinator.model.WriteResult
import com.axondragonscale.devinator.permission.SecureSettingsPermission
import com.axondragonscale.devinator.repository.DevSettingsRepository
import com.axondragonscale.devinator.repository.TimedToggleRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Created by Ronak on 08/11/26 */

/**
 * The two timed tiles share their lifecycle and differ only in the state they apply. A tap starts
 * a timed session; tapping the tile that owns the active session ends it early, and tapping the
 * opposite tile switches direction.
 */
abstract class TimedDevOptionsTileService : TileService(), KoinComponent {

    protected val timedToggle: TimedToggleRepository by inject()
    private val settings: DevSettingsRepository by inject()

    private var listening: Job? = null

    /** False for the Pause tile, true for the Resume tile. */
    protected abstract val target: Boolean

    private val targetLabel: String get() = if (target) "On" else "Off"

    override fun onStartListening() {
        super.onStartListening()
        listening?.cancel()
        listening = CoroutineScope(Dispatchers.Main.immediate).launch {
            // A tile is only bound while the shade is open, so a one-second tick is cheap and
            // keeps the countdown honest.
            while (true) {
                render()
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
        CoroutineScope(Dispatchers.IO).launch {
            val active = timedToggle.session.value
            val result = if (active?.target == target) {
                timedToggle.endSession()
            } else {
                timedToggle.toggle(target)
            }
            when (result) {
                is WriteResult.Failed -> toast("Couldn't change this setting.")
                null -> toast(
                    if (target) "Developer options is already on." else "Developer options is already off.",
                )

                else -> Unit
            }
        }
    }

    private fun render() {
        val tile = qsTile ?: return
        if (!SecureSettingsPermission.isGranted(this)) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.subtitle = "Grant access"
            tile.updateTile()
            return
        }

        val session = timedToggle.session.value
        val developerOptions = settings.state.value.developerOptions
        val activeSession = session?.takeIf { it.target == target }

        tile.state = if (activeSession != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = when {
            activeSession != null -> remaining(activeSession.deadline)?.let { "$targetLabel · $it" } ?: targetLabel
            developerOptions == target -> targetLabel
            else -> "$targetLabel for ${timedToggle.duration.value.label}"
        }
        tile.updateTile()
    }

    private fun remaining(deadline: Instant?): String? {
        val target = deadline ?: return null
        val seconds = (target - Clock.System.now()).inWholeSeconds
        if (seconds <= 0) return null
        return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
    }

    private suspend fun toast(message: String) = withContext(Dispatchers.Main) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}

class PauseDevOptionsTileService : TimedDevOptionsTileService() {
    override val target: Boolean = false
}

class ResumeDevOptionsTileService : TimedDevOptionsTileService() {
    override val target: Boolean = true
}
