import kotlin.jvm.JvmInline

@JvmInline
value class TLVList private constructor(
    val tlvList: MutableList<TLV>
) {
    companion object {

        fun from(tlvList: MutableList<TLV>): TLVList =
            TLVList(tlvList)

        fun from(tlvBytes: ByteArray): TLVList {
            val result = mutableListOf<TLV>()
            var offset = 0
            while (offset < tlvBytes.size) {
                // Skip padding bytes (0x00 or 0xFF)
                if (tlvBytes[offset] == 0x00.toByte() || tlvBytes[offset] == 0xFF.toByte()) {
                    offset++
                    continue
                }
                val tlv = TLV.from(tlvBytes.copyOfRange(offset, tlvBytes.size))
                result.add(tlv)
                offset += tlv.size
            }
            return TLVList(result)
        }

    }

    val size get() = tlvList.sumOf { it.size }
    val bytes get() = tlvList.fold(byteArrayOf()) { acc, tlv -> acc + tlv.bytes }
    val hex get() = tlvList.fold("") { acc, tlv -> acc + tlv.hex }

}
