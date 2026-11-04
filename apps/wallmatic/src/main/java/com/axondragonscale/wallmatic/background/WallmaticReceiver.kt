package com.axondragonscale.wallmatic.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.commons.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.measureTimeMillis

/**
 * This receiver makes sure the wallpaper update chain exists after system events.
 *
 * It listens for the following system broadcasts:
 * - **ACTION_BOOT_COMPLETED:** Re-establishes the WorkManager chain when the device reboots.
 * - **ACTION_TIME_CHANGED:** Makes sure the chain exists after the device time is changed.
 * - **ACTION_TIMEZONE_CHANGED:** Same as above, after a timezone change.
 * - **ACTION_MY_PACKAGE_REPLACED:** Revives both the update chain and the watchdog after an
 *   app update wipes all pending WorkManager work.
 *
 * WorkManager survives reboots natively, but this ensures a chain wiped by a system event is
 * revived immediately rather than waiting for the watchdog.
 */
class WallmaticReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val TAG = "WallmaticReceiver"
    }

    private val scheduler: WallpaperWorkScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        TAG.logD { "Broadcast received: ${intent.action}" }

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // The schedule is derived from `lastUpdated` in the config, so the receiver's job
                // is only to make sure the chain exists. `ensureScheduled` (KEEP) is idempotent
                // and never cancels or resets a run that is already pending or in flight; a clock
                // change needs no immediate reschedule because the next run re-evaluates the due
                // check itself.
                scheduler.ensureScheduled()
                scheduler.scheduleWatchdog()
            }
        }
    }

    private fun BroadcastReceiver.goAsync(block: suspend () -> Unit) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val time = measureTimeMillis { block() }
            TAG.logD { "Broadcast processing time: $time ms" }
            pendingResult.finish()
        }
    }

}
