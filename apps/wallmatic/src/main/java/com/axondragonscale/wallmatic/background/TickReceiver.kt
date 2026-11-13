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

/**
 * The only entry point for scheduled work: the tick alarm itself, plus the system events that
 * wipe it (alarms do not survive a reboot or an app update).
 */
class TickReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_TICK = "com.axondragonscale.wallmatic.action.TICK"
        private const val TAG = "TickReceiver"

        private val HANDLED_ACTIONS = setOf(
            ACTION_TICK,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }

    private val updateCoordinator: UpdateCoordinator by inject()
    private val tickScheduler: TickScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in HANDLED_ACTIONS) return
        TAG.logD { "onReceive: $action" }

        // The alarm that fired is already consumed. Arm a replacement before anything can suspend,
        // so a process death during the update cannot leave no wake-up at all; applyIfDue refines it.
        tickScheduler.armAt(System.currentTimeMillis() + UpdateCoordinator.MAX_RETRY_DELAY.inWholeMilliseconds)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateCoordinator.applyIfDue()
            } finally {
                pendingResult.finish()
            }
        }
    }

}
