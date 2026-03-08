package com.axondragonscale.limitless.model

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * A value class wrapping an [Int] bitmask representing a set of days in a week.
 * Uses ISO day numbering: 1 = Monday .. 7 = Sunday (bit 0 = Mon .. bit 6 = Sun).
 */
@kotlin.jvm.JvmInline
value class BitWeek(val value: Int = 0) {

    companion object {
        val EMPTY = BitWeek(0)

        /**
         * Creates a [BitWeek] with the given ISO day numbers (1=Mon .. 7=Sun) selected.
         */
        fun of(vararg isoDayNumbers: Int): BitWeek =
            isoDayNumbers.fold(EMPTY) { acc, day -> acc.toggleDay(day) }
    }

    /**
     * Returns true if the given ISO day number (1=Mon .. 7=Sun) is selected.
     */
    fun isDaySelected(isoDayNumber: Int): Boolean =
        value and (1 shl (isoDayNumber - 1)) != 0

    /**
     * Toggles the given ISO day number and returns the new [BitWeek].
     */
    fun toggleDay(isoDayNumber: Int): BitWeek =
        BitWeek(value xor (1 shl (isoDayNumber - 1)))

    /**
     * Returns all selected ISO day numbers (1=Mon .. 7=Sun), sorted.
     */
    fun selectedDays(): List<Int> =
        (1..7).filter { isDaySelected(it) }

    fun isEmpty(): Boolean = value == 0
}
