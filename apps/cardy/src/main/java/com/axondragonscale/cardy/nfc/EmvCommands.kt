package com.axondragonscale.cardy.nfc

import com.axondragonscale.cardy.util.hexToBytes

/** Created by Ronak on 05/30/26 */
object EmvCommands {

    /** SELECT Payment System Environment (2PAY.SYS.DDF01) */
    fun selectPpse(): ByteArray =
        "00A404000E325041592E5359532E444446303100".hexToBytes()

    /** SELECT application by AID */
    fun selectAid(aid: ByteArray): ByteArray {
        val header = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte())
        return header + aid + byteArrayOf(0x00)
    }

    /** GET PROCESSING OPTIONS with PDOL data */
    fun getProcessingOptions(pdolData: ByteArray? = null): ByteArray {
        val data = if (pdolData != null) {
            // Tag 83 + length + PDOL data
            byteArrayOf(0x83.toByte(), pdolData.size.toByte()) + pdolData
        } else {
            // Empty PDOL: tag 83, length 00
            byteArrayOf(0x83.toByte(), 0x00)
        }
        val header = byteArrayOf(0x80.toByte(), 0xA8.toByte(), 0x00, 0x00, data.size.toByte())
        return header + data + byteArrayOf(0x00)
    }

    /** READ RECORD command */
    fun readRecord(recordNumber: Int, sfi: Int): ByteArray {
        val p2 = (sfi shl 3) or 0x04
        return byteArrayOf(0x00, 0xB2.toByte(), recordNumber.toByte(), p2.toByte(), 0x00)
    }
}
