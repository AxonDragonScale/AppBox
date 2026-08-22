package com.axondragonscale.cardy.nfc

import com.axondragonscale.cardy.util.toHex

/** Created by Ronak on 05/30/26 */
class ApduResponse(
    val data: ByteArray,
    val sw1: Int,
    val sw2: Int,
) {
    companion object {
        fun from(rawResponse: ByteArray): ApduResponse {
            require(rawResponse.size >= 2) { "APDU response must be at least 2 bytes" }
            val data = rawResponse.copyOfRange(0, rawResponse.size - 2)
            val sw1 = rawResponse[rawResponse.size - 2].toInt() and 0xFF
            val sw2 = rawResponse[rawResponse.size - 1].toInt() and 0xFF
            return ApduResponse(data, sw1, sw2)
        }
    }

    val isSuccess: Boolean get() = sw1 == 0x90 && sw2 == 0x00
    val statusWord: String get() = String.format("%02X%02X", sw1, sw2)
    val dataHex: String get() = data.toHex()
}
