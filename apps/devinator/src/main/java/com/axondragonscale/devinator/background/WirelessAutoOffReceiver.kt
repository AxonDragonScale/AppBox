package com.axondragonscale.devinator.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.commons.log
import com.axondragonscale.devinator.repository.DevSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Created by Ronak on 11/06/26 */

/**
 * The alarm's only job: turn wireless debugging off through the repository, so the resulting value
 * arrives the same way every other write does.
 */
class WirelessAutoOffReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: DevSettingsRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.setWirelessDebugging(false)
            } catch (e: Exception) {
                log { e }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
