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
 * This receiver is responsible for rescheduling wallpaper updates after system events.
 *
 * It listens for the following system broadcasts:
 * - **ACTION_BOOT_COMPLETED:** Re-kicks the WorkManager chain when the device reboots.
 * - **ACTION_TIME_CHANGED:** Reschedules the update when the device time is changed.
 * - **ACTION_TIMEZONE_CHANGED:** Reschedules the update when the device timezone is changed.
 *
 * WorkManager survives reboots natively, but this ensures any missed or drifted chain is
 * revived immediately after a system event rather than waiting for the watchdog.
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
            Intent.ACTION_TIMEZONE_CHANGED -> scheduler.scheduleNextUpdate()
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
