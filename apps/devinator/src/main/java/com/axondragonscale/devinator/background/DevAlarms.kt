package com.axondragonscale.devinator.background

import android.content.Context

/** Created by Ronak on 08/11/26 */

/** The app's two one-shot alarms, kept apart by request code. */
class DevAlarms(context: Context) {

    val autoOff = DeadlineAlarm(context, AUTO_OFF_REQUEST, WirelessAutoOffReceiver::class.java)

    val timedToggle = DeadlineAlarm(context, TIMED_TOGGLE_REQUEST, TimedToggleReceiver::class.java)

    private companion object {
        const val AUTO_OFF_REQUEST = 0
        const val TIMED_TOGGLE_REQUEST = 1
    }
}
