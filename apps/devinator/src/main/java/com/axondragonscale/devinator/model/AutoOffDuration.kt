package com.axondragonscale.devinator.model

/** Created by Ronak on 11/06/26 */

/**
 * The auto-off choices the UI offers, and the persisted form of that setting.
 *
 * An enum rather than a nullable minute count: "off" is a choice the user makes, not a missing
 * value, and it has to survive in the same single preference as the durations.
 *
 * @property minutes null when the timer is off
 */
enum class AutoOffDuration(val minutes: Int?) {
    OFF(null),
    MIN_5(5),
    MIN_15(15),
    MIN_30(30),
}
