@file:OptIn(ExperimentalStdlibApi::class)

import kotlin.experimental.and

data class TLV(
    val tag: Tag,
    val length: Length,
    val value: Value,
) {
    companion object {

        fun from(tag: Tag, value: Value): TLV =
            TLV(tag = tag, length = Length.from(value.size), value = value)

        fun from(tlvHex: String): TLV =
            from(tlvHex.hexToByteArray())

        fun from(tlvBytes: ByteArray): TLV {
            require(tlvBytes.isNotEmpty()) { "TLV bytes must not be empty" }

            val tag = if (tlvBytes.first() and 0b0001_1111.toByte() != 0b0001_1111.toByte()) {
                Tag.from(byteArrayOf(tlvBytes.first()))
            } else {
                val terminatorIndex = tlvBytes.drop(1).indexOfFirst { it and 0b1000_0000.toByte() == 0.toByte() }
                require(terminatorIndex >= 0) { "Multi-byte tag has no terminator byte" }
                val lastTagByteIndex = 1 + terminatorIndex
                Tag.from(tlvBytes.sliceArray(0 .. lastTagByteIndex))
            }

            val lengthStartIndex = tag.size
            require(lengthStartIndex < tlvBytes.size) { "TLV bytes too short: missing length field" }

            val lengthEndIndex = lengthStartIndex + if (tlvBytes[lengthStartIndex] and 0b1000_0000.toByte() == 0.toByte()) {
                0
            } else {
                tlvBytes[lengthStartIndex].toInt() and 0b0111_1111
            }
            require(lengthEndIndex < tlvBytes.size) { "TLV bytes too short: length field truncated" }
            val length = Length.from(tlvBytes.sliceArray(lengthStartIndex .. lengthEndIndex))

            val valueStartIndex = lengthEndIndex + 1
            val valueEndIndex = lengthEndIndex + length.lengthAsInt
            require(valueEndIndex < tlvBytes.size) { "TLV bytes too short: value truncated (need ${length.lengthAsInt} bytes, have ${tlvBytes.size - valueStartIndex})" }
            val value = Value(tlvBytes.sliceArray(valueStartIndex .. valueEndIndex))

            return TLV(tag = tag, length = length, value = value)
        }

    }

    val size get() = tag.size + length.size + value.size
    val bytes get() = tag.tagBytes + length.lengthBytes + value.valueBytes
    val hex get() = bytes.toHexString()

    val isNested get() = tag.isConstructed
    val nestedTlvs: TLVList get() = TLVList.from(value.valueBytes)

}
