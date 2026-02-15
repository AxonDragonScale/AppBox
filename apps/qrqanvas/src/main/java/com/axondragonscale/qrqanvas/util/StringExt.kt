package com.axondragonscale.qrqanvas.util

/**
 * Created by Ronak Harkhani on 09/07/25
 */


/** Characters that must be backslash-escaped in WiFi QR values. */
val WIFI_ESCAPE_CHARS = charArrayOf(';', ',', ':', '"')

/**
 * Escapes each character in [chars] by prefixing it with a backslash.
 * The backslash character itself is always escaped first to avoid double-escaping.
 *
 * Example: `"hello;world".escapeWithBackslash(charArrayOf(';'))` → `"hello\\;world"`
 */
fun String.escapeBackslash(chars: CharArray): String {
    var result = this.replace("\\", "\\\\")
    for (ch in chars) {
        result = result.replace("$ch", "\\$ch")
    }
    return result
}

/**
 * Unescapes backslash-escaped characters in [chars], plus the backslash itself.
 * Reverses the operation performed by [escapeBackslash].
 *
 * Example: `"hello\\;world".unescapeBackslash(charArrayOf(';'))` → `"hello;world"`
 */
fun String.unescapeBackslash(chars: CharArray): String {
    var result = this
    for (ch in chars) {
        result = result.replace("\\$ch", "$ch")
    }
    result = result.replace("\\\\", "\\")
    return result
}
