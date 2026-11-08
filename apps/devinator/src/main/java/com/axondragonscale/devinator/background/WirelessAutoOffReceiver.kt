package com.axondragonscale.devinator.background

import android.content.Context
import android.content.Intent
import com.axondragonscale.devinator.repository.DevSettingsRepository
import org.koin.core.component.inject

/** Created by Ronak on 11/06/26 */

/**
 * The alarm's only job: turn wireless debugging off through the repository, so the resulting value
 * arrives the same way every other write does.
 */
class WirelessAutoOffReceiver : KoinReceiver() {

    private val repository: DevSettingsRepository by inject()

    override suspend fun onReceiveSuspend(context: Context, intent: Intent) = repository.onAutoOffAlarm()
}
