package com.axondragonscale.storageviewer.data

/** Created by Ronak on 14/11/26 */

private const val EMPTY_TEXT = "\"\""

/** A value as a row shows it: [text] is capped, [byteCount] is the size before the cap. */
internal data class DisplayText(val text: String, val byteCount: Int, val truncated: Boolean)

/**
 * Caps [value] at [maxBytes] for a row. An empty string renders as `""` so it stays distinct from
 * an absent key, and [byteCount] keeps the uncapped size so a cut row can say what it lost.
 */
internal fun displayedText(value: String, maxBytes: Int = CELL_BYTES): DisplayText {
    if (value.isEmpty()) return DisplayText(EMPTY_TEXT, EMPTY_TEXT.length, false)
    val bytes = value.toByteArray(Charsets.UTF_8)
    if (bytes.size <= maxBytes) return DisplayText(value, bytes.size, false)
    val cut = String(bytes, 0, utf8Cut(bytes, maxBytes), Charsets.UTF_8)
    return DisplayText(cut, bytes.size, true)
}

/** Renders a string set as `["a", "b"]` in iteration order; embedded quotes are left as they are. */
internal fun setDisplay(values: Iterable<*>): String =
    values.joinToString(prefix = "[", postfix = "]", separator = ", ") { value -> "\"$value\"" }

// Cuts on a code-point boundary so a capped value never ends in half a UTF-8 sequence.
private fun utf8Cut(bytes: ByteArray, maxBytes: Int): Int {
    var end = maxBytes
    while (end > 0 && (bytes[end].toInt() and 0xC0) == 0x80) end--
    return end
}
