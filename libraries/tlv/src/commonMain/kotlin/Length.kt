@file:OptIn(ExperimentalStdlibApi::class)

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.jvm.JvmInline

@JvmInline
value class Length private constructor(val lengthBytes: ByteArray) {

    companion object Companion {

        fun isValid(lengthBytes: ByteArray): Boolean {
            if (lengthBytes.isEmpty()) return false

            if (lengthBytes.size == 1) {
                return lengthBytes.first() and 0b1000_0000.toByte() == 0b0.toByte()
            }

            if (lengthBytes.first() and 0b1000_0000.toByte() == 0b0.toByte()) return false

            val len = lengthBytes.first() and 0b0111_1111.toByte()
            return len + 1 == lengthBytes.size
        }

        fun isValid(lengthHex: String): Boolean =
            isValid(lengthHex.hexToByteArray())

        fun from(lengthBytes: ByteArray): Length {
            check(isValid(lengthBytes)) { "Invalid lengthBytes" }
            return Length(lengthBytes)
        }

        fun from(lengthHex: String): Length =
            from(lengthHex.hexToByteArray())

        fun from(length: Long): Length {
            check(length >= 0) { "Length must be non-negative" }

            if (length <= 0b0111_1111)
                return from(byteArrayOf(length.toByte()))

            val actualLengthBytes = length.toMinimalByteArray()
            val firstByte = actualLengthBytes.size.toByte() or 0b1000_0000.toByte()
            return from(byteArrayOf(firstByte) + actualLengthBytes)
        }

        fun from(length: Int): Length =
            from(length.toLong())

    }

    val size get() = lengthBytes.size

    val lengthAsBytes: ByteArray
        get() {
            if (lengthBytes.isEmpty()) return byteArrayOf(0)

            if (lengthBytes.size == 1) {
                check(lengthBytes.first() and 0b1000_0000.toByte() == 0b0.toByte()) { "Invalid lengthBytes" }
                return lengthBytes
            }

            val len = lengthBytes.first() and 0b0111_1111.toByte()
            check(len + 1 == lengthBytes.size) { "Invalid lengthBytes" }
            return lengthBytes.drop(1).toByteArray()
        }

    val lengthAsLong: Long
        get() = lengthAsBytes.toLong()

    val lengthAsInt: Int
        get() = lengthAsBytes.toInt()

    val lengthAsHex: String
        get() = lengthAsBytes.toHexString()

}
