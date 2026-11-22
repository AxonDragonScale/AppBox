package com.axondragonscale.logviewer.model

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * The level vocabulary every source maps into. A source's own spelling is folded into this ramp.
 */
enum class LogLevel(val letter: Char, val label: String) {
    TRACE('V', "Verbose"),
    DEBUG('D', "Debug"),
    INFO('I', "Info"),
    WARN('W', "Warn"),
    ERROR('E', "Error"),
    FATAL('F', "Fatal"),
}
