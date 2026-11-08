package com.axondragonscale.devinator.model

/** Created by Ronak on 08/11/26 */

/**
 * The timed-toggle choices the UI offers. This is deliberately separate from [AutoOffDuration]:
 * "Off" is not a valid timed toggle, every option has a positive duration.
 */
enum class TimedToggleDuration(val minutes: Int) {
    MIN_5(5),
    MIN_15(15),
    MIN_30(30),
    ;

    val label: String get() = "$minutes min"
}
