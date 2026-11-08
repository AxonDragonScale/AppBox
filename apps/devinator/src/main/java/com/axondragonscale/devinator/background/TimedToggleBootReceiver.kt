package com.axondragonscale.devinator.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.commons.log
import com.axondragonscale.devinator.repository.TimedToggleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Created by Ronak on 08/11/26 */

/**
 * A reboot ends the "bank app" session, so restoring the pre-session state is safer than leaving
 * Developer options off with a dead alarm. A force-stop cannot be caught here; the repository
 * reconciles on the next app start instead.
 */
class TimedToggleBootReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: TimedToggleRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.restoreAfterReboot()
            } catch (e: Exception) {
                log { e }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
