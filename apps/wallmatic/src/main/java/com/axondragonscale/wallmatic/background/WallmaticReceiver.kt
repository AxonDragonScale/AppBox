package com.axondragonscale.wallmatic.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import com.axondragonscale.commons.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.measureTimeMillis

/**
 * This receiver is responsible for rescheduling wallpaper changes and triggering immediate updates.
 *
 * It listens for the following system broadcasts:
 * - **ACTION_BOOT_COMPLETED:** Reschedules wallpaper updates when the device reboots.
 * - **ACTION_TIME_CHANGED:** Reschedules wallpaper updates when the device time is changed.
 * - **ACTION_TIMEZONE_CHANGED:** Reschedules wallpaper updates when the device timezone is changed.
 *
 * It listens for the following custom broadcasts:
 * - **ACTION_CHANGE_WALLPAPER:**  Triggers an immediate wallpaper update.
 */
class WallmaticReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val TAG = "WallmaticReceiver"
        private const val ACTION_CHANGE_WALLPAPER = "com.axondragonscale.wallmatic.CHANGE_WALLPAPER"

        fun getWallpaperChangeIntent(context: Context) =
            Intent(context, WallmaticReceiver::class.java).apply {
                action = ACTION_CHANGE_WALLPAPER
            }
    }

    private val scheduler: WallmaticScheduler by inject()
    private val wallpaperUpdater: WallpaperUpdater by inject()

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        TAG.logD { "Broadcast received: ${intent.action}" }

        when (intent.action) {
            // TODO: Will this exceed the 10s time limit
            // Should this just start the service instead
            ACTION_CHANGE_WALLPAPER -> wallpaperUpdater.updateWallpaper()

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
