import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun `byte conversion to int or long`() {
        // 0 sign bit
        assertTrue { 0b0100_1000.toByte().toInt() == 0b0100_1000 }
        assertTrue { 0b0100_1000.toByte().asInt() == 0b0100_1000 }
        assertTrue { 0b0100_1000.toByte().asLong() == 0b0100_1000L }

        // 1 sign bit
        assertFalse { 0b1000_1000.toByte().toInt() == 0b1000_1000 }
        assertTrue { 0b1000_1000.toByte().asInt() == 0b1000_1000 }
        assertTrue { 0b1000_1000.toByte().asLong() == 0b1000_1000L }
    }

    @Test
    fun `bytearray conversion to int or long`() {
        // empty bytearray
        assertTrue { byteArrayOf().toInt() == 0 }
        assertTrue { byteArrayOf().toLong() == 0L }

        // bytearray of size 1
        assertTrue { byteArrayOf(0b0000_1000).toInt() == 8 }
        assertTrue { byteArrayOf(0b0000_1000).toLong() == 8L }

        // bytearray of size 4
        assertTrue { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0).toInt() == 134217728 }
        assertTrue { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0).toLong() == 134217728L }

        // bytearray of size 5
        assertFails { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0, 0b0).toInt() }
        assertTrue { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0, 0b0).toLong() == 34359738368L }

        // bytearray of size 8
        assertFails { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0).toInt() }
        assertTrue { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0).toLong() == 576460752303423488L }

        // bytearray of size 9
        assertFails { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0).toInt() }
        assertFails { byteArrayOf(0b0000_1000, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0, 0b0).toLong() }
    }

    @Test
    fun `long conversion to bytearray`() {
        // 0
        0L.toByteArray().let {
            assertTrue { it.toLong() == 0L }
            assertTrue { it.contentEquals(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0)) }
            assertTrue { it.size == 8 }
        }
        0L.toMinimalByteArray().let {
            assertTrue { it.toLong() == 0L }
            assertTrue { it.contentEquals(byteArrayOf(0)) }
            assertTrue { it.size == 1 }
        }

        // 2 bytes
        0x10_00L.toByteArray().let {
            assertTrue { it.toLong() == 0x10_00L }
            assertTrue { it.contentEquals(byteArrayOf(0, 0, 0, 0, 0, 0, 0x10, 0)) }
            assertTrue { it.size == 8 }
        }
        0x10_00L.toMinimalByteArray().let {
            assertTrue { it.toLong() == 0x10_00L }
            assertTrue { it.contentEquals(byteArrayOf(0x10, 0)) }
            assertTrue { it.size == 2 }
        }

        // 7 bytes
        0x10_00_00_00_00_00_00L.toByteArray().let {
            assertTrue { it.toLong() == 0x10_00_00_00_00_00_00L }
            assertTrue { it.contentEquals(byteArrayOf(0, 0x10, 0, 0, 0, 0, 0, 0)) }
            assertTrue { it.size == 8 }
        }
        0x10_00_00_00_00_00_00L.toMinimalByteArray().let {
            assertTrue { it.toLong() == 0x10_00_00_00_00_00_00L }
            assertTrue { it.contentEquals(byteArrayOf(0x10, 0, 0, 0, 0, 0, 0)) }
            assertTrue { it.size == 7 }
        }

        // 7 bytes
        0x10_00_00_00_00_00_00_00L.toByteArray().let {
            assertTrue { it.toLong() == 0x10_00_00_00_00_00_00_00L }
            assertTrue { it.contentEquals(byteArrayOf(0x10, 0, 0, 0, 0, 0, 0, 0)) }
            assertTrue { it.size == 8 }
        }
        0x10_00_00_00_00_00_00_00L.toMinimalByteArray().let {
            assertTrue { it.toLong() == 0x10_00_00_00_00_00_00_00L }
            assertTrue { it.contentEquals(byteArrayOf(0x10, 0, 0, 0, 0, 0, 0, 0)) }
            assertTrue { it.size == 8 }
        }
    }

}