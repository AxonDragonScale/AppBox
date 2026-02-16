import kotlin.test.Test
import kotlin.test.assertEquals

class LengthTest {

    @Test
    fun `length validity scenarios`() {
        // Length of single byte must have 0 MSB
        assertEquals(false, Length.isValid(byteArrayOf(0b1000_0000.toByte())))
        assertEquals(false, Length.isValid(byteArrayOf(0b1000_0001.toByte())))
        assertEquals(false, Length.isValid(byteArrayOf(0b1111_1111.toByte())))

        // Length of single byte with 0 MSB can have any length between 0 - 127
        assertEquals(true, Length.isValid(byteArrayOf(0b0000_0000.toByte())))
        assertEquals(true, Length.isValid(byteArrayOf(0b0000_0001.toByte())))
        assertEquals(true, Length.isValid(byteArrayOf(0b0111_1111.toByte())))

        // Length of multiple bytes must have 1 MSB
        assertEquals(false, Length.isValid(byteArrayOf(0b0000_0001, 0b0000_0001)))
        assertEquals(false, Length.isValid(byteArrayOf(0b0000_0000, 0b0000_0001)))

        // Length of multiple bytes must have length as given by last 7 bits of 1st byte
        assertEquals(false, Length.isValid(byteArrayOf(0b1000_0000.toByte(), 0b0000_0001)))
        assertEquals(false, Length.isValid(byteArrayOf(0b1000_0001.toByte(), 0b0000_0000, 0b0000_0001)))

        // Length of multiple bytes with length as given by last 7 bits of 1st byte can have length
        assertEquals(true, Length.isValid(byteArrayOf(0b1000_0001.toByte(), 0b0000_0001)))
        assertEquals(true, Length.isValid(byteArrayOf(0b1000_0001.toByte(), 0b1111_1111.toByte())))
        assertEquals(true, Length.isValid(byteArrayOf(0b1000_0010.toByte(), 0b0000_0000, 0b0000_0001)))
        assertEquals(true, Length.isValid(byteArrayOf(0b1000_0011.toByte(), 0b0000_0000, 0b0000_0000, 0b0000_0001)))
        assertEquals(true, Length.isValid(byteArrayOf(0b1000_0100.toByte(), 0b0000_0000, 0b0000_0000, 0b0000_0000, 0b0000_0001)))
    }

    @Test
    fun `length from length bytes or hex`() {
        // Length of single byte
        val length1 = Length.from(byteArrayOf(0b0000_0001))
        assertEquals(true, length1.size == 1)
        assertEquals(true, length1.lengthAsBytes.contentEquals(byteArrayOf(0b0000_0001.toByte())))
        assertEquals(true, length1.lengthAsInt == 1)
        assertEquals(true, length1.lengthAsLong == 1L)
        assertEquals(true, length1.lengthAsHex == "01")

        // Length of single byte
        val length2 = Length.from(byteArrayOf(0b0000_0100))
        assertEquals(true, length2.size == 1)
        assertEquals(true, length2.lengthAsBytes.contentEquals(byteArrayOf(0b0000_0100.toByte())))
        assertEquals(true, length2.lengthAsInt == 4)
        assertEquals(true, length2.lengthAsLong == 4L)
        assertEquals(true, length2.lengthAsHex == "04")

        // Length of single byte
        val length3 = Length.from(byteArrayOf(0b0111_1111))
        assertEquals(true, length3.size == 1)
        assertEquals(true, length3.lengthAsBytes.contentEquals(byteArrayOf(0b0111_1111.toByte())))
        assertEquals(true, length3.lengthAsInt == 127)
        assertEquals(true, length3.lengthAsLong == 127L)
        assertEquals(true, length3.lengthAsHex == "7f")

        // Length of 2 bytes
        val length4 = Length.from(byteArrayOf(0b1000_0001.toByte(), 0b0000_0010))
        assertEquals(true, length4.size == 2)
        assertEquals(true, length4.lengthAsBytes.contentEquals(byteArrayOf(0b0000_0010)))
        assertEquals(true, length4.lengthAsInt == 2)
        assertEquals(true, length4.lengthAsLong == 2L)
        assertEquals(true, length4.lengthAsHex == "02")

        // Length of 3 bytes
        val length5 = Length.from(byteArrayOf(0b1000_0010.toByte(), 0b0000_0001, 0b0000_0001))
        assertEquals(true, length5.size == 3)
        assertEquals(true, length5.lengthAsBytes.contentEquals(byteArrayOf(0b0000_0001, 0b0000_0001)))
        assertEquals(true, length5.lengthAsInt == 257)
        assertEquals(true, length5.lengthAsLong == 257L)
        assertEquals(true, length5.lengthAsHex == "0101")
    }

    @Test
    fun `length from actual length`() {
        // Length of single byte with minimum value
        val length1 = Length.from(1)
        assertEquals(true, length1.size == 1)
        assertEquals(true, length1.lengthBytes.contentEquals(byteArrayOf(0b0000_0001.toByte())))
        assertEquals(true, length1.lengthAsBytes.contentEquals(byteArrayOf(0b0000_0001.toByte())))
        assertEquals(true, length1.lengthAsInt == 1)
        assertEquals(true, length1.lengthAsLong == 1L)
        assertEquals(true, length1.lengthAsHex == "01")

        // Length of single byte with minimum value
        val length2 = Length.from(127)
        assertEquals(true, length2.size == 1)
        assertEquals(true, length2.lengthBytes.contentEquals(byteArrayOf(0b0111_1111.toByte())))
        assertEquals(true, length2.lengthAsBytes.contentEquals(byteArrayOf(0b0111_1111.toByte())))
        assertEquals(true, length2.lengthAsInt == 127)
        assertEquals(true, length2.lengthAsLong == 127L)
        assertEquals(true, length2.lengthAsHex == "7f")

        // Length of 2 bytes
        val length3 = Length.from(128)
        assertEquals(true, length3.size == 2)
        assertEquals(true, length3.lengthBytes.contentEquals(byteArrayOf(0b1000_0001.toByte(), 0b1000_0000.toByte())))
        assertEquals(true, length3.lengthAsBytes.contentEquals(byteArrayOf(0b1000_0000.toByte())))
        assertEquals(true, length3.lengthAsInt == 128)
        assertEquals(true, length3.lengthAsLong == 128L)
        assertEquals(true, length3.lengthAsHex == "80")

        // Length of 2 bytes with max value
        val length4 = Length.from(255)
        assertEquals(true, length4.size == 2)
        assertEquals(true, length4.lengthBytes.contentEquals(byteArrayOf(0b1000_0001.toByte(), 0b1111_1111.toByte())))
        assertEquals(true, length4.lengthAsBytes.contentEquals(byteArrayOf(0b1111_1111.toByte())))
        assertEquals(true, length4.lengthAsInt == 255)
        assertEquals(true, length4.lengthAsLong == 255L)
        assertEquals(true, length4.lengthAsHex == "ff")

        // Length of 3 bytes
        val length5 = Length.from(256)
        assertEquals(true, length5.size == 3)
        assertEquals(true, length5.lengthBytes.contentEquals(byteArrayOf(0b1000_0010.toByte(), 0b0000_0001, 0b0000_0000)))
        assertEquals(true, length5.lengthAsBytes.contentEquals(byteArrayOf(0b0000_0001, 0b0000_0000)))
        assertEquals(true, length5.lengthAsInt == 256)
        assertEquals(true, length5.lengthAsLong == 256L)
        assertEquals(true, length5.lengthAsHex == "0100")
    }

}