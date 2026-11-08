package com.axondragonscale.devinator.model

import kotlin.time.Clock
import kotlin.time.Instant

/** Created by Ronak on 08/11/26 */

/** `m:ss` until [this]; `0:00` once the deadline has passed, null when there is no deadline. */
fun Instant?.remainingText(now: Instant = Clock.System.now()): String? {
    val deadline = this ?: return null
    val seconds = (deadline - now).inWholeSeconds.coerceAtLeast(0)
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

fun Instant?.hasTimeLeft(now: Instant = Clock.System.now()): Boolean = this != null && this > now
