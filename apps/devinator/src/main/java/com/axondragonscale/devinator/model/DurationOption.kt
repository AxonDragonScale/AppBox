package com.axondragonscale.devinator.model

/** Created by Ronak on 08/11/26 */

/**
 * The durations both timers offer. [OFF] belongs only to the wireless auto-off; the timed toggle
 * offers [timed].
 */
enum class DurationOption(val minutes: Int?) {
    OFF(null),
    MIN_5(5),
    MIN_15(15),
    MIN_30(30),
    ;

    val label: String get() = minutes?.let { "$it min" } ?: "Off"

    companion object {
        val timed: List<DurationOption> = entries.filter { it.minutes != null }
    }
}
