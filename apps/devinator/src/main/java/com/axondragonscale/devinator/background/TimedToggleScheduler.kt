package com.axondragonscale.devinator.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.axondragonscale.commons.log
import com.axondragonscale.devinator.repository.AppPrefsRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Created by Ronak on 08/11/26 */

/**
 * Arms the one-shot alarm that ends a timed toggle and mirrors the deadline the UI counts down
 * from.
 *
 * `setAndAllowWhileIdle` needs no permission and is still delivered in Doze; an exact alarm would
 * need `SCHEDULE_EXACT_ALARM`, which Android 14+ denies by default. A failing alarm is logged here
 * rather than handed to the caller: losing the timer must not stop the settings from being observed.
 */
class TimedToggleScheduler(
    context: Context,
    private val prefs: AppPrefsRepository,
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Request code 1 keeps this alarm separate from WirelessAutoOffReceiver's request code 0.
    private val pendingIntent = PendingIntent.getBroadcast(
        context,
        1,
        Intent(context, TimedToggleReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private val _deadline = MutableStateFlow<Instant?>(null)

    /** Null when no timed-toggle session is armed. */
    val deadline: StateFlow<Instant?> = _deadline.asStateFlow()

    /** Loads the persisted deadline, so the countdown survives process death. */
    suspend fun restoreDeadline() {
        _deadline.value = prefs.timedToggleDeadline.get()?.let(Instant::fromEpochMilliseconds)
    }

    suspend fun armAt(at: Instant) {
        val triggerAt = at.toEpochMilliseconds().coerceAtLeast(System.currentTimeMillis())
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (e: Exception) {
            log { e }
        }
        prefs.timedToggleDeadline.set(triggerAt)
        _deadline.value = Instant.fromEpochMilliseconds(triggerAt)
    }

    suspend fun cancel() {
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            log { e }
        }
        prefs.timedToggleDeadline.set(null)
        _deadline.value = null
    }
}
