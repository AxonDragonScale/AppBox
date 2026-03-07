package com.axondragonscale.commons

/**
 * Created by Ronak Harkhani on 23 January 2026.
 *
 * Common UI mode enum for controlling light/dark theme across all apps.
 */
enum class UIMode(val displayName: String) {
    LIGHT("Light"),
    AUTO("Auto"),
    DARK("Dark");

    companion object {
        val DEFAULT get() = AUTO

        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
