package com.axondragonscale.limitless.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * BroadcastReceiver that listens for [Intent.ACTION_BOOT_COMPLETED] to reschedule
 * all habit reminders after a device reboot.
 *
 * AlarmManager alarms do not survive reboots, so this receiver re-registers
 * all pending reminders from the database via [ReminderScheduler].
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val scheduler: ReminderScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduler.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
