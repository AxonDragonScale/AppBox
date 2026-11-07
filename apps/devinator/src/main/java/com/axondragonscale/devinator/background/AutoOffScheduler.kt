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

/** Created by Ronak on 11/06/26 */

/**
 * Arms the one-shot alarm that switches wireless debugging off, and mirrors the deadline the UI
 * counts down from.
 *
 * `setAndAllowWhileIdle` needs no permission and is still delivered in Doze; an exact alarm would
 * need `SCHEDULE_EXACT_ALARM`, which Android 14+ denies by default. A failing alarm is logged here
 * rather than handed to the caller: losing the timer must not stop the settings from being observed.
 */
class AutoOffScheduler(
    context: Context,
    private val prefs: AppPrefsRepository,
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, WirelessAutoOffReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private val _deadline = MutableStateFlow<Instant?>(null)

    /** Null when no timer is armed. */
    val deadline: StateFlow<Instant?> = _deadline.asStateFlow()

    /** Loads the persisted deadline, so the countdown survives process death. */
    suspend fun restoreDeadline() {
        _deadline.value = prefs.autoOffDeadline.get()?.let(Instant::fromEpochMilliseconds)
    }

    suspend fun armAt(at: Instant) {
        val triggerAt = at.toEpochMilliseconds().coerceAtLeast(System.currentTimeMillis())
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (e: Exception) {
            log { e }
        }
        prefs.autoOffDeadline.set(triggerAt)
        _deadline.value = Instant.fromEpochMilliseconds(triggerAt)
    }

    suspend fun cancel() {
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            log { e }
        }
        prefs.autoOffDeadline.set(null)
        _deadline.value = null
    }
}
