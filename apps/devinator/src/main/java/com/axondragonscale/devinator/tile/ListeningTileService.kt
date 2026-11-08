package com.axondragonscale.devinator.tile

import android.service.quicksettings.TileService
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

/** Created by Ronak on 08/11/26 */

internal const val GRANT_ACCESS_LABEL = "Grant access"
internal const val DEV_OPTIONS_OFF_LABEL = "Dev options off"

/** The lifecycle every tile shares: render once a second while the shade holds it. */
abstract class ListeningTileService : TileService(), KoinComponent {

    private var listening: Job? = null

    protected abstract fun render()

    /** A tile's own deadline check; Home and the shade must heal a dropped alarm the same way. */
    protected open suspend fun reconcile() = Unit

    override fun onStartListening() {
        super.onStartListening()
        listening?.cancel()
        listening = CoroutineScope(Dispatchers.Main.immediate).launch {
            // A tile is only bound while the shade is open, so a one-second tick is cheap and
            // keeps a countdown honest.
            while (true) {
                reconcile()
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

    protected suspend fun toast(message: String) = withContext(Dispatchers.Main) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
