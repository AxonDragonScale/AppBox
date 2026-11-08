package com.axondragonscale.devinator.background

import android.content.Context
import android.content.Intent
import com.axondragonscale.devinator.repository.TimedToggleRepository
import org.koin.core.component.inject

/** Created by Ronak on 08/11/26 */

/**
 * The alarm's only job: end the timed session through the repository, so the restore state arrives
 * the same way every other write does.
 */
class TimedToggleReceiver : KoinReceiver() {

    private val repository: TimedToggleRepository by inject()

    override suspend fun onReceiveSuspend(context: Context, intent: Intent) {
        repository.endSession()
    }
}
