@file:OptIn(ExperimentalStdlibApi::class)

import kotlin.jvm.JvmInline

@JvmInline
value class Value(val valueBytes: ByteArray) {

    companion object {
        fun from(valueBytes: ByteArray): Value =
            Value(valueBytes)

        fun from(valueHex: String): Value =
            Value(valueHex.hexToByteArray())
    }

    val size get() = valueBytes.size
    val asHex get() = valueBytes.toHexString()
}