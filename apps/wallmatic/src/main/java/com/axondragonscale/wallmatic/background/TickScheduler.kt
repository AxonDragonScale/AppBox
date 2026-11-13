package com.axondragonscale.wallmatic.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.axondragonscale.commons.logD
import java.time.Instant

/**
 * Owns the single alarm that drives the auto-cycle.
 *
 * `setAndAllowWhileIdle` needs no permission and is still delivered during Doze, where it is
 * throttled to roughly the interval we schedule anyway. Exact alarms would need
 * `SCHEDULE_EXACT_ALARM`, which Android 14+ denies by default.
 */
class TickScheduler(context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, TickReceiver::class.java).setAction(TickReceiver.ACTION_TICK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** Replaces the pending tick. */
    fun armAt(triggerAtMillis: Long) {
        val now = System.currentTimeMillis()
        val triggerAt = triggerAtMillis.coerceAtLeast(now)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        this.logD { "Tick armed for ${Instant.ofEpochMilli(triggerAt)} (delay: ${triggerAt - now}ms)" }
    }

    fun cancel() {
        alarmManager.cancel(pendingIntent)
        this.logD { "Tick cancelled" }
    }

}
