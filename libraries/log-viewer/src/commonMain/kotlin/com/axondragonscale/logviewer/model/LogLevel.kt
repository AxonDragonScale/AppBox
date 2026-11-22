package com.axondragonscale.logviewer.model

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * The level vocabulary every source maps into. A source's own spelling is folded into this ramp.
 *
 * [letter] is the console glyph, not a wire format: logcat writes `A` for an assert, which shows
 * here as [FATAL]'s `F`.
 */
enum class LogLevel(val letter: Char, val label: String) {
    TRACE('V', "Verbose"),
    DEBUG('D', "Debug"),
    INFO('I', "Info"),
    WARN('W', "Warn"),
    ERROR('E', "Error"),
    FATAL('F', "Fatal"),
}
