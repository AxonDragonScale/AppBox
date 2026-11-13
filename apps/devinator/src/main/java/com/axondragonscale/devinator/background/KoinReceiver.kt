package com.axondragonscale.devinator.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.commons.catching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/** Created by Ronak on 08/11/26 */

/**
 * Every alarm receiver needs the same `goAsync` window and the same give-up-on-exception rule; the
 * work itself is the only part that differs.
 */
abstract class KoinReceiver : BroadcastReceiver(), KoinComponent {

    final override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            this@KoinReceiver.catching(onFinally = { pendingResult.finish() }) {
                onReceiveSuspend(context, intent)
            }
        }
    }

    protected abstract suspend fun onReceiveSuspend(context: Context, intent: Intent)
}
