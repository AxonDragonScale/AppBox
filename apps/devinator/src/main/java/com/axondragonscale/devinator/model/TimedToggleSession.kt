package com.axondragonscale.devinator.model

import kotlin.time.Instant

/** Created by Ronak on 08/11/26 */

/**
 * One armed timed toggle. [target] is the state applied for the session; [restoreTo] is the state
 * that comes back at [deadline].
 */
data class TimedToggleSession(
    val target: Boolean,
    val restoreTo: Boolean,
    val deadline: Instant,
)
