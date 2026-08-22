package com.axondragonscale.cardy.emv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EmvTagTest {

    @Test
    fun resolvesStandardAndUnknownTags() {
        assertEquals(EmvTag.APPLICATION_PAN, EmvTag.resolve("5a"))
        assertIs<UnknownEmvTag>(EmvTag.resolve("DF01"))
        assertEquals("DF01", EmvTag.resolve("df01").hex)
    }
}
