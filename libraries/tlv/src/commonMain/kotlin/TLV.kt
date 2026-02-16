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
            val tag = if (tlvBytes.first() and 0b0001_1111.toByte() != 0b0001_1111.toByte()) {
                Tag.from(byteArrayOf(tlvBytes.first()))
            } else {
                val lastTagByteIndex = 1 + tlvBytes.drop(1).indexOfFirst { it and 0b1000_0000.toByte() == 0.toByte() }
                Tag.from(tlvBytes.sliceArray(0 .. lastTagByteIndex))
            }

            val lengthStartIndex = tag.size
            val lengthEndIndex = lengthStartIndex + if (tlvBytes[lengthStartIndex] and 0b1000_0000.toByte() == 0.toByte()) {
                0
            } else {
                tlvBytes[lengthStartIndex].toInt() and 0b0111_1111
            }
            val length = Length.from(tlvBytes.sliceArray(lengthStartIndex until lengthEndIndex))

            val valueEndIndex = lengthEndIndex + length.lengthAsInt
            val value = Value(tlvBytes.sliceArray(lengthEndIndex + 1 .. valueEndIndex))

            return TLV(tag = tag, length = length, value = value)
        }

    }

    val size get() = tag.size + length.size + value.size
    val bytes get() = tag.tagBytes + length.lengthBytes + value.valueBytes
    val hex get() = bytes.toHexString()

    val isNested get() = tag.isConstructed
    val nestedTlvs: TLVList get() = TLVList.from(value.valueBytes)

}