package com.axondragonscale.cardy.emv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Track2EquivalentDataTest {

    @Test
    fun parsesAllTrack2ComponentsAndPadding() {
        val parsed = parseTrack2EquivalentData(
            "5413330089012345D2912201123456789F".hexToByteArray(),
        )

        assertEquals("5413330089012345", parsed?.pan)
        assertEquals("2912", parsed?.expirationDate)
        assertEquals("201", parsed?.serviceCode)
        assertEquals("123456789", parsed?.discretionaryData)
    }

    @Test
    fun rejectsMissingFieldSeparator() {
        assertNull(parseTrack2EquivalentData("5413330089012345".hexToByteArray()))
    }
}
