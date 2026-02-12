package com.axondragonscale.npinner.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.npinner.core.NPinnerNotificationMonitor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 01/05/23
 */

class AppUpdateReceiver : BroadcastReceiver(), KoinComponent {

    private val notificationMonitor: NPinnerNotificationMonitor by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        GlobalScope.launch {
            notificationMonitor.ensurePinnedNotificationVisibility()
        }
    }

}
