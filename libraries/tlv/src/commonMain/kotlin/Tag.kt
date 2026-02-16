@file:OptIn(ExperimentalStdlibApi::class)

import kotlin.experimental.and
import kotlin.jvm.JvmInline
import kotlin.math.ceil

/**
 * Tag represents the tag in Tag-Length-Value
 */
@JvmInline
value class Tag private constructor(val tagBytes: ByteArray) {

    /**
     * Class represents the type of the tag in terms of its usage
     */
    enum class Class(val value: Byte) {
       Universal(0b00),
       Application(0b01),
       ContextSpecific(0b10),
       Private(0b11);

        companion object {
            fun fromValue(value: Byte): Class = entries.first { it.value == value }
        }

    }

    /**
     * Form represents the type of the `value` in TLV
     */
    enum class Form(val value: Byte) {

        /**
         * Primitive - directly represent the encoded value
         */
        Primitive(0b0),

        /**
         * Constructed - value has nested TLVs
         */
        Constructed(0b1);

        companion object {
            fun fromValue(value: Byte): Form = entries.first { it.value == value }
        }
    }

    companion object {

        fun isValidTag(tagHex: String): Boolean =
            isValidTag(tagHex.hexToByteArray())

        fun isValidTag(tagBytes: ByteArray): Boolean {
            if (tagBytes.isEmpty()) return false

            // If the tag number is 0, its invalid
            if (tagBytes.size == 1 && tagBytes.first() and 0b0001_1111.toByte() == 0b0000_0000.toByte()) return false

            // If the last 5 bits of first byte are 1, then size must be greater than 1
            if (tagBytes.size == 1 && tagBytes.first() and 0b0001_1111.toByte() == 0b0001_1111.toByte()) return false

            // If the size > 1, then the last 5 bits of the first byte must be set
            if (tagBytes.size > 1 && tagBytes.first() and 0b0001_1111.toByte() != 0b0001_1111.toByte()) return false

            // If the size > 1, then only the last byte's MSB must be 0
            tagBytes.forEachIndexed { index, byte ->
                if (index == 0) return@forEachIndexed
                if (index == tagBytes.lastIndex && byte and 0b1000_0000.toByte() == 0b1000_0000.toByte()) return false
                if (index != tagBytes.lastIndex && byte and 0b1000_0000.toByte() != 0b1000_0000.toByte()) return false
            }

            return true
        }

        fun from(tagHex: String): Tag =
            from(tagHex.hexToByteArray())

        fun from(tagBytes: ByteArray): Tag {
            check(isValidTag(tagBytes)) { "Tag is invalid" }
            return Tag(tagBytes)
        }

        fun from(
            tagNumber: Long,
            tagClass: Class = Class.Universal,
            tagForm: Form = Form.Primitive,
        ): Tag =
            from(
                tagNumberBytes = tagNumber.toMinimalByteArray(),
                tagClass = tagClass,
                tagForm = tagForm
            )

        fun from(
            tagNumberHex: String,
            tagClass: Class = Class.Universal,
            tagForm: Form = Form.Primitive,
        ): Tag =
            from(
                tagNumberBytes = tagNumberHex.hexToByteArray(),
                tagClass = tagClass,
                tagForm = tagForm
            )

        fun from(
            tagNumberBytes: ByteArray,
            tagClass: Class = Class.Universal,
            tagForm: Form = Form.Primitive,
        ): Tag {
            require(tagNumberBytes.isNotEmpty()) { "TagNumber is empty" }

            if (tagNumberBytes.size == 1 && tagNumberBytes.first().asInt() < 0b0001_1111) {
                val tagBytes = (tagClass.value.asInt() shl 6) or (tagForm.value.asInt() shl 5) or tagNumberBytes.first().asInt()
                return Tag(byteArrayOf(tagBytes.toByte()))
            }

            val firstByte = ((tagClass.value.asInt() shl 6) or (tagForm.value.asInt() shl 5) or 0b0001_1111).toByte()

            if (tagNumberBytes.size == 1 && tagNumberBytes.first().asInt() and 0b1000_0000 == 0b0000_0000) {
                return Tag(byteArrayOf(firstByte, tagNumberBytes.first()))
            }

            val originalBits = tagNumberBytes.size * 8
            val extraBits = ceil(originalBits / 7.0).toInt()
            val tagBytesSize = 1 + ceil((originalBits + extraBits) / 8.0).toInt()
            val tagBytes = ByteArray(tagBytesSize)
            tagBytes[0] = firstByte

            var curIndex = tagBytes.lastIndex
            var curByte = 0
            var bitsFilled = 0
            tagNumberBytes.reversed().forEach { byte ->
                curByte = curByte or (byte.asInt() shl bitsFilled)
                bitsFilled += 8

                while (bitsFilled >= 7) {
                    val value = (curByte and 0b0111_1111) or if (curIndex == tagBytes.lastIndex) 0b0000_0000 else 0b1000_0000
                    tagBytes[curIndex--] = value.toByte()
                    curByte = curByte ushr 7
                    bitsFilled -= 7
                }
            }

            if (bitsFilled > 0 && curIndex > 0) {
                tagBytes[curIndex] = (curByte or 0b1000_0000).toByte()
            }

            return Tag(tagBytes)
        }

    }

    val tagClass: Class get() = Class.fromValue((tagBytes.first().asInt() shr 6).toByte() and 0b11) // First 2 bits indicate class
    val isUniversal get() = tagClass == Class.Universal
    val isApplication get() = tagClass == Class.Application
    val isContextSpecific get() = tagClass == Class.ContextSpecific
    val isPrivate get() = tagClass == Class.Private

    val tagForm: Form get() = Form.fromValue((tagBytes.first().asInt() shr 5).toByte() and 0b001) // 3rd bit indicates form
    val isPrimitive get() = tagForm == Form.Primitive
    val isConstructed get() = tagForm == Form.Constructed

    /**
     * The size of the tag in bytes
     */
    val size get() = tagBytes.size

    val asBytes: ByteArray get() = tagBytes
    val asHex: String get() = tagBytes.toHexString()

    val tagNumberBytes: ByteArray get() = getNumberBytes()
    val tagNumberLong: Long get() = tagNumberBytes.toLong()
    val tagNumberInt: Int get() = tagNumberBytes.toInt()

    private fun getNumberBytes(): ByteArray {
        if (tagBytes.size == 1) return byteArrayOf(tagBytes.first() and 0b0001_1111.toByte())

        // For every byte, 7 bits are required, 1 is discarded
        val resultSize = ceil(((tagBytes.size - 1) * 7) / 8.0).toInt()
        val result = ByteArray(resultSize)

        var curByte = 0
        var bitsFilled = 0
        var curIndex = result.lastIndex
        tagBytes.drop(1).reversed().forEach { byte ->
            val value = byte.asInt() and 0b0111_1111
            curByte = curByte or (value shl bitsFilled)
            bitsFilled += 7

            if (bitsFilled >= 8) {
                result[curIndex--] = curByte.toByte()
                bitsFilled -= 8
                curByte = curByte ushr 8
            }
        }

        if (bitsFilled > 0 && curIndex >= 0) {
            result[curIndex] = curByte.toByte()
        }

        return result
    }

}
