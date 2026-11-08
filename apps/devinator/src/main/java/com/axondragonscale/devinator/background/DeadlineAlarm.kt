package com.axondragonscale.devinator.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.axondragonscale.commons.log
import kotlin.time.Instant

/** Created by Ronak on 08/11/26 */

/**
 * One one-shot alarm. The repository that owns the timer persists its deadline; this only arms and
 * cancels.
 *
 * `setAndAllowWhileIdle` is inexact but needs no permission and still fires in Doze; an exact alarm
 * would require `SCHEDULE_EXACT_ALARM`, which Android 14+ denies by default.
 */
class DeadlineAlarm(
    context: Context,
    requestCode: Int,
    receiver: Class<out BroadcastReceiver>,
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        Intent(context, receiver),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun armAt(at: Instant) {
        val triggerAt = at.toEpochMilliseconds().coerceAtLeast(System.currentTimeMillis())
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (e: Exception) {
            log { e }
        }
    }

    fun cancel() {
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            log { e }
        }
    }
}
