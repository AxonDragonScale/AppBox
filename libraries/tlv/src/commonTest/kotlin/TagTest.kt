@file:OptIn(ExperimentalStdlibApi::class)

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TagTest {

    @Test
    fun `tag validity scenarios`() {
        val testCases = listOf(
            // Empty tag
            Triple(
                "tag must not be empty",
                byteArrayOf(),
                false
            ),
            
            // Tags with non-zero number
            Triple(
                "tag must have non-zero number",
                byteArrayOf(0b0000_0000),
                false
            ),
            Triple(
                "tag must have non-zero number",
                byteArrayOf(0b1110_0000.toByte()),
                false
            ),
            
            // Tag with number 31
            Triple(
                "tag with number 31 (last 5 bits set) must have following bytes",
                byteArrayOf(0b0001_1111),
                false
            ),
            
            // Single byte tags with non-zero number
            Triple(
                "tag with single byte can have any class and form with non-zero number",
                byteArrayOf(0b1110_0001.toByte()),
                true
            ),
            Triple(
                "tag with single byte can have any class and form with non-zero number",
                byteArrayOf(0b0000_0001.toByte()),
                true
            ),
            Triple(
                "tag with single byte can have any class and form with non-zero number",
                byteArrayOf(0b0110_0001.toByte()),
                true
            ),
            Triple(
                "tag with single byte can have any class and form with non-zero number",
                byteArrayOf(0b1000_0001.toByte()),
                true
            ),
            
            // Tags with size > 1 must have last 5 bits set
            Triple(
                "tag with size > 1 must have last 5 bits set",
                byteArrayOf(0b0001_1110, 0b0000_0001),
                false
            ),
            Triple(
                "tag with size > 1 must have last 5 bits set",
                byteArrayOf(0b0000_1111, 0b0000_0001),
                false
            ),
            
            // Tags with multiple bytes must have MSB 0 in the last byte
            Triple(
                "tag with multiple bytes must have MSB 0 in the last byte",
                byteArrayOf(0b0001_1111, 0b1000_0000.toByte()),
                false
            ),
            Triple(
                "tag with multiple bytes must have MSB 0 in the last byte",
                byteArrayOf(0b0001_1111, 0b1000_0000.toByte(), 0b1000_0000.toByte()),
                false
            ),
            
            // Tags with multiple bytes must have MSB 1 in the non last bytes
            Triple(
                "tag with multiple bytes must have MSB 1 in the non last bytes",
                byteArrayOf(0b0001_1111, 0b0000_0001, 0b0000_0001),
                false
            ),
            Triple(
                "tag with multiple bytes must have MSB 1 in the non last bytes",
                byteArrayOf(0b0001_1111, 0b0000_0001, 0b1000_0001.toByte(), 0b0000_0001),
                false
            ),
            
            // Tags with 2 bytes and MSB 0 in the last byte
            Triple(
                "tag with 2 bytes and MSB 0 in the last byte can have any non-zero number",
                byteArrayOf(0b0001_1111, 0b0000_0001),
                true
            ),
            Triple(
                "tag with 2 bytes and MSB 0 in the last byte can have any non-zero number",
                byteArrayOf(0b0001_1111, 0b0111_1111),
                true
            ),
            
            // Tags with 3 bytes, MSB 1 in middle byte, MSB 0 in the last byte
            Triple(
                "tag with 3 bytes, MSB 1 in middle byte, MSB 0 in the last byte can have any non-zero number",
                byteArrayOf(0b0001_1111, 0b1000_0000.toByte(), 0b0000_0001),
                true
            ),
            Triple(
                "tag with 3 bytes, MSB 1 in middle byte, MSB 0 in the last byte can have any non-zero number",
                byteArrayOf(0b0001_1111, 0b1111_1111.toByte(), 0b0111_1111),
                true
            )
        )
        
        testCases.forEach { testCase ->
            val (msg, bytes, expected) = testCase
            assertEquals(expected, Tag.isValidTag(bytes), msg)
            assertEquals(expected, Tag.isValidTag(bytes.toHexString()), msg)
        }
    }

    @Test
    fun `tag from full bytes or hex`() {
        // Single Byte tag | Universal class | Primitive Form
        val tag1 = Tag.from(byteArrayOf(0b0000_0001.toByte()))
        assertEquals(Tag.Class.Universal, tag1.tagClass)
        assertEquals(Tag.Form.Primitive, tag1.tagForm)
        assertEquals(0b1, tag1.tagNumberBytes.first())

        // Single Byte tag | Application class | Constructed Form
        val tag2 = Tag.from(byteArrayOf(0b0110_0111.toByte()))
        assertEquals(Tag.Class.Application, tag2.tagClass)
        assertEquals(Tag.Form.Constructed, tag2.tagForm)
        assertEquals(0b111, tag2.tagNumberBytes.first())

        // Single Byte tag | ContextSpecific class | Constructed Form
        val tag3 = Tag.from(byteArrayOf(0b1010_1111.toByte()))
        assertEquals(Tag.Class.ContextSpecific, tag3.tagClass)
        assertEquals(Tag.Form.Constructed, tag3.tagForm)
        assertEquals(0b1111, tag3.tagNumberBytes.first())

        // Single Byte tag | Private class | Primitive Form
        val tag4 = Tag.from(byteArrayOf(0b1100_1001.toByte()))
        assertEquals(Tag.Class.Private, tag4.tagClass)
        assertEquals(Tag.Form.Primitive, tag4.tagForm)
        assertEquals(0b1001, tag4.tagNumberBytes.first())

        // Two Bytes | Application Class | Constructed Form
        val tag5 = Tag.from(byteArrayOf(0b0111_1111, 0b0111_1111))
        assertEquals(Tag.Class.Application, tag5.tagClass)
        assertEquals(Tag.Form.Constructed, tag5.tagForm)
        assertEquals(0b0111_1111, tag5.tagNumberBytes.toInt())

        // Three Bytes | Universal Class | Primitive Form
        val tag6 = Tag.from(byteArrayOf(0b0001_1111, 0b1010_1010.toByte(), 0b0101_0101))
        assertEquals(Tag.Class.Universal, tag6.tagClass)
        assertEquals(Tag.Form.Primitive, tag6.tagForm)
        assertEquals(0b0101010_1010101, tag6.tagNumberBytes.toInt())

        // Four Bytes | Universal Class | Primitive Form
        val tag7 = Tag.from(byteArrayOf(0b0001_1111, 0b1101_0101.toByte(), 0b1010_1010.toByte(), 0b0101_0101))
        assertEquals(Tag.Class.Universal, tag7.tagClass)
        assertEquals(Tag.Form.Primitive, tag7.tagForm)
        assertEquals(0b1010101_0101010_1010101, tag7.tagNumberBytes.toInt())

        // Ten Bytes | Universal Class | Primitive Form
        val tag8 = Tag.from(byteArrayOf(0b0001_1111, 0b1111_1111.toByte(), 0b1111_1111.toByte(), 0b1111_1111.toByte(), 0b1111_1111.toByte(), 0b1111_1111.toByte(), 0b1111_1111.toByte(), 0b1111_1111.toByte(), 0b1111_1111.toByte(), 0b0111_1111.toByte()))
        assertEquals(Tag.Class.Universal, tag8.tagClass)
        assertEquals(Tag.Form.Primitive, tag8.tagForm)
        assertContentEquals(byteArrayOf(0b0111_1111, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()), tag8.tagNumberBytes)
    }

    @Test
    fun `tag from tagNumberBytes`() {
        // Single Byte | Min value
        val tag1 = Tag.from(byteArrayOf(0b0_0001), Tag.Class.Universal, Tag.Form.Primitive)
        assertEquals(Tag.Class.Universal, tag1.tagClass)
        assertEquals(Tag.Form.Primitive, tag1.tagForm)
        assertEquals(0b0_0001.toByte(), tag1.tagNumberBytes.first())
        assertEquals(1, tag1.size)

        // Single Byte | Max value
        val tag2 = Tag.from(byteArrayOf(0b1_1110.toByte()), Tag.Class.Application, Tag.Form.Constructed)
        assertEquals(Tag.Class.Application, tag2.tagClass)
        assertEquals(Tag.Form.Constructed, tag2.tagForm)
        assertEquals(0b1_1110.toByte(), tag2.tagNumberBytes.first())
        assertEquals(1, tag2.size)

        // Non-Single Byte | Single max value + 1
        val tag3 = Tag.from(byteArrayOf(0b1_1111.toByte()), Tag.Class.ContextSpecific, Tag.Form.Primitive)
        assertEquals(Tag.Class.ContextSpecific, tag3.tagClass)
        assertEquals(Tag.Form.Primitive, tag3.tagForm)
        assertEquals(0b1_1111.toByte(), tag3.tagNumberBytes.first())
        assertEquals(2, tag3.size)

        // 4 Bytes
        val tag4 = Tag.from(byteArrayOf(0xFF.toByte(), 0xFF.toByte()), Tag.Class.Private, Tag.Form.Constructed)
        assertEquals(Tag.Class.Private, tag4.tagClass)
        assertEquals(Tag.Form.Constructed, tag4.tagForm)
        assertEquals(0xFFFF, tag4.tagNumberBytes.toInt())
        assertEquals(4, tag4.size)
    }

}
