package com.axondragonscale.devinator.tile

import android.service.quicksettings.Tile
import com.axondragonscale.devinator.model.WRITE_FAILED_MESSAGE
import com.axondragonscale.devinator.model.WriteResult
import com.axondragonscale.devinator.model.hasTimeLeft
import com.axondragonscale.devinator.model.remainingText
import com.axondragonscale.devinator.permission.GrantRepository
import com.axondragonscale.devinator.repository.DevSettingsRepository
import com.axondragonscale.devinator.repository.TimedToggleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/** Created by Ronak on 08/11/26 */

/**
 * The two timed tiles share their lifecycle and differ only in the state they apply. A tap starts
 * a timed session; tapping the tile that owns the active session ends it early, and tapping the
 * opposite tile switches direction.
 */
abstract class TimedDevOptionsTileService : ListeningTileService() {

    protected val timedToggle: TimedToggleRepository by inject()
    private val settings: DevSettingsRepository by inject()
    private val grant: GrantRepository by inject()

    /** False for the Pause tile, true for the Resume tile. */
    protected abstract val target: Boolean

    private val targetLabel: String get() = if (target) "On" else "Off"

    override fun onClick() {
        CoroutineScope(Dispatchers.IO).launch {
            val active = timedToggle.session.value
            val result = if (active?.target == target) {
                timedToggle.endSession()
            } else {
                timedToggle.toggle(target)
            }
            when (result) {
                WriteResult.Failed -> toast(WRITE_FAILED_MESSAGE)
                null -> toast(
                    if (target) "Developer options is already on." else "Developer options is already off.",
                )

                else -> Unit
            }
        }
    }

    override suspend fun reconcile() = timedToggle.reconcileDeadline()

    override fun render() {
        val tile = qsTile ?: return
        if (!grant.isGranted()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.subtitle = GRANT_ACCESS_LABEL
            tile.updateTile()
            return
        }

        val session = timedToggle.session.value
        val developerOptions = settings.state.value.developerOptions
        val activeSession = session?.takeIf { it.target == target }
        val deadline = activeSession?.deadline
        val remaining = if (deadline.hasTimeLeft()) deadline.remainingText() else null

        tile.state = if (activeSession != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = when {
            activeSession != null -> remaining?.let { "$targetLabel · $it" } ?: targetLabel
            developerOptions == target -> targetLabel
            else -> "$targetLabel for ${timedToggle.duration.value.label}"
        }
        tile.updateTile()
    }
}

class PauseDevOptionsTileService : TimedDevOptionsTileService() {
    override val target: Boolean = false
}

class ResumeDevOptionsTileService : TimedDevOptionsTileService() {
    override val target: Boolean = true
}
