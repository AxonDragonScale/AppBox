package com.axondragonscale.wallmatic.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.math.abs

/**
 * Created by Ronak Harkhani on 12/11/26
 */

/** The single definition of which intervals exist and what they are called. */
object Interval {

    data class Tick(val duration: Duration, val label: String)

    val minimum: Duration = 5.minutes
    val maximum: Duration = 168.hours
    val default: Duration = 15.minutes

    val ticks: List<Tick> = listOf(
        Tick(default, "15m"),
        Tick(30.minutes, "30m"),
        Tick(1.hours, "1h"),
        Tick(3.hours, "3h"),
        Tick(6.hours, "6h"),
        Tick(12.hours, "12h"),
        Tick(24.hours, "24h"),
    )

    fun coerce(duration: Duration): Duration = duration.coerceIn(minimum, maximum)

    fun nearestTickIndex(duration: Duration): Int = ticks
        .mapIndexed { index, tick -> index to abs((tick.duration - duration).inWholeSeconds) }
        .minBy { it.second }
        .first

    fun label(duration: Duration): String {
        val h = duration.inWholeHours
        val m = duration.inWholeMinutes % 60
        return when {
            h == 0L -> "$m MIN"
            m == 0L -> "$h HR"
            else -> "$h HR $m MIN"
        }
    }
}
