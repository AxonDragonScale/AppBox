package com.axondragonscale.devinator.background

import android.content.Context
import android.content.Intent
import com.axondragonscale.devinator.repository.DevSettingsRepository
import com.axondragonscale.devinator.repository.TimedToggleRepository
import org.koin.core.component.inject

/** Created by Ronak on 08/11/26 */

/**
 * A reboot ends the "bank app" timed session, so the pre-session state is restored; the wireless
 * auto-off is reconciled from its persisted deadline. A force-stop cannot be caught here; both
 * repositories reconcile again on the next app start.
 */
class DevBootReceiver : KoinReceiver() {

    private val settings: DevSettingsRepository by inject()
    private val timedToggle: TimedToggleRepository by inject()

    override suspend fun onReceiveSuspend(context: Context, intent: Intent) {
        settings.reconcileAfterBoot()
        timedToggle.restoreAfterReboot()
    }
}
