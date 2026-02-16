import kotlin.jvm.JvmInline

@JvmInline
value class TLVList private constructor(
    val tlvList: MutableList<TLV>
) {
    companion object {

        fun from(tlvList: MutableList<TLV>): TLVList =
            TLVList(tlvList)

        fun from(tlvList: ByteArray): TLVList {
            val result = mutableListOf<TLV>()

            var remainingSize = tlvList.size
            while (remainingSize > 0) {
                val tlv = TLV.from(tlvList)
                result.add(tlv)
                remainingSize -= tlv.size
            }

            return TLVList(result)
        }

    }

    val size get() = tlvList.sumOf { it.size }
    val bytes get() = tlvList.fold(byteArrayOf()) { acc, tlv -> acc + tlv.bytes }
    val hex get() = tlvList.fold("") { acc, tlv -> acc + tlv.hex }

}
