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
 * The alarm's only job: end the timed session through the repository, so the restore state arrives
 * the same way every other write does.
 */
class TimedToggleReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: TimedToggleRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.endSession()
            } catch (e: Exception) {
                log { e }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
