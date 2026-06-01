package com.axondragonscale.cardy.nfc

import TLV
import android.nfc.tech.IsoDep
import com.axondragonscale.cardy.emv.CardNetwork
import com.axondragonscale.cardy.emv.EmvDataExtractor
import com.axondragonscale.cardy.model.CardReadResult
import com.axondragonscale.cardy.model.EmvApplication
import com.axondragonscale.cardy.util.toHex
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE

/** Created by Ronak on 05/30/26 */
private const val TAG = "CardReader"

class CardReader {

    fun readCard(isoDep: IsoDep): Result<CardReadResult> = runCatching {
        TAG.logD { "readCard: starting card read" }
        isoDep.connect()
        isoDep.timeout = 5000

        try {
            val applications = mutableListOf<EmvApplication>()

            // Step 1: SELECT PPSE
            TAG.logD { "readCard: Step 1 - selecting PPSE" }
            val ppseResponse = transceive(isoDep, EmvCommands.selectPpse())
            if (!ppseResponse.isSuccess) {
                TAG.logE { "readCard: PPSE selection failed with status ${ppseResponse.statusWord}" }
                error("PPSE selection failed: ${ppseResponse.statusWord}")
            }
            TAG.logD { "readCard: PPSE selection succeeded" }

            // Step 2: Extract AIDs from PPSE response
            TAG.logD { "readCard: Step 2 - extracting AIDs from PPSE response" }
            val aids = EmvDataExtractor.extractAidsFromPpse(ppseResponse.data)
            if (aids.isEmpty()) {
                TAG.logE { "readCard: no AIDs found in PPSE response" }
                error("No AIDs found in PPSE response")
            }
            TAG.logD { "readCard: found ${aids.size} AID(s): ${aids.joinToString { it.toHex() }}" }

            // Step 3: For each AID, perform full read
            TAG.logD { "readCard: Step 3 - reading each application" }
            for (aid in aids) {
                TAG.logD { "readCard: reading application for AID ${aid.toHex()}" }
                val app = readApplication(isoDep, aid)
                if (app != null) {
                    TAG.logD { "readCard: successfully read application ${app.label ?: app.aid} (${app.network})" }
                    applications.add(app)
                } else {
                    TAG.logD { "readCard: skipped AID ${aid.toHex()} (null result)" }
                }
            }

            if (applications.isEmpty()) {
                TAG.logE { "readCard: no applications could be read from card" }
                error("No applications could be read from card")
            }

            // Build result using primary application (first with PAN, or first overall)
            val primary = applications.firstOrNull { it.pan != null } ?: applications.first()
            TAG.logD { "readCard: primary application is ${primary.label ?: primary.aid} (${primary.network}), PAN present=${primary.pan != null}" }

            val result = CardReadResult(
                applications = applications,
                primaryNetwork = primary.network,
                primaryPan = primary.pan,
                primaryExpiry = primary.expiry,
                cardholderName = primary.cardholderName ?: applications.firstNotNullOfOrNull { it.cardholderName },
                timestamp = System.currentTimeMillis(),
            )
            TAG.logD { "readCard: complete - ${applications.size} app(s), network=${result.primaryNetwork}, expiry=${result.primaryExpiry}" }
            result
        } finally {
            try { isoDep.close() } catch (_: Exception) {}
        }
    }

    private fun readApplication(isoDep: IsoDep, aid: ByteArray): EmvApplication? {
        val aidHex = aid.toHex()
        TAG.logD { "readApplication: AID=$aidHex" }
        val allTlvs = mutableListOf<TLV>()

        // SELECT AID
        val selectResponse = transceive(isoDep, EmvCommands.selectAid(aid))
        if (!selectResponse.isSuccess) {
            TAG.logD { "readApplication: SELECT AID failed for $aidHex (status=${selectResponse.statusWord})" }
            return null
        }
        TAG.logD { "readApplication: SELECT AID succeeded for $aidHex" }

        val selectTlvs = EmvDataExtractor.parseTlvData(selectResponse.data)
        allTlvs.addAll(selectTlvs)

        // GET PROCESSING OPTIONS
        val pdol = EmvDataExtractor.findTag(selectTlvs, "9F38")
        val pdolData = pdol?.let { buildPdolData(it.value.valueBytes) }
        TAG.logD { "readApplication: PDOL ${if (pdol != null) "found (${pdolData?.size} bytes)" else "not found, using empty GPO"}" }

        val gpoResponse = transceive(isoDep, EmvCommands.getProcessingOptions(pdolData))
        if (gpoResponse.isSuccess && gpoResponse.data.isNotEmpty()) {
            TAG.logD { "readApplication: GPO succeeded for $aidHex" }
            val gpoTlvs = EmvDataExtractor.parseTlvData(gpoResponse.data)
            allTlvs.addAll(gpoTlvs)

            // Parse AFL and read records
            val aflBytes = extractAfl(gpoTlvs, gpoResponse.data)
            if (aflBytes != null) {
                TAG.logD { "readApplication: AFL found (${aflBytes.size} bytes), reading records" }
                val recordTlvs = readRecords(isoDep, aflBytes)
                TAG.logD { "readApplication: read ${recordTlvs.size} TLV(s) from records" }
                allTlvs.addAll(recordTlvs)
            } else {
                TAG.logD { "readApplication: no AFL found for $aidHex" }
            }
        } else {
            TAG.logD { "readApplication: GPO failed or empty for $aidHex (status=${gpoResponse.statusWord})" }
        }

        return EmvDataExtractor.extractApplicationData(allTlvs, aid)
    }

    private fun extractAfl(gpoTlvs: List<TLV>, rawData: ByteArray): ByteArray? {
        // Format 2 (tag 77): AFL is in tag 94
        val aflTag = EmvDataExtractor.findTag(gpoTlvs, "94")
        if (aflTag != null) {
            TAG.logD { "extractAfl: Format 2 (tag 94) AFL found, ${aflTag.value.valueBytes.size} bytes" }
            return aflTag.value.valueBytes
        }

        // Format 1 (tag 80): AIP (2 bytes) + AFL (rest)
        val format1 = EmvDataExtractor.findTag(gpoTlvs, "80")
        if (format1 != null && format1.value.valueBytes.size > 2) {
            TAG.logD { "extractAfl: Format 1 (tag 80) AFL found, ${format1.value.valueBytes.size - 2} bytes (after AIP)" }
            return format1.value.valueBytes.copyOfRange(2, format1.value.valueBytes.size)
        }

        TAG.logD { "extractAfl: no AFL found in GPO response" }
        return null
    }

    private fun readRecords(isoDep: IsoDep, afl: ByteArray): List<TLV> {
        val tlvs = mutableListOf<TLV>()
        // AFL is groups of 4 bytes: [SFI byte, first record, last record, offline records]
        var i = 0
        while (i + 3 < afl.size) {
            val sfi = (afl[i].toInt() and 0xFF) shr 3
            val firstRecord = afl[i + 1].toInt() and 0xFF
            val lastRecord = afl[i + 2].toInt() and 0xFF
            TAG.logD { "readRecords: SFI=$sfi records $firstRecord..$lastRecord" }
            i += 4

            for (record in firstRecord..lastRecord) {
                val response = transceive(isoDep, EmvCommands.readRecord(record, sfi))
                if (response.isSuccess && response.data.isNotEmpty()) {
                    val recordTlvs = EmvDataExtractor.parseTlvData(response.data)
                    TAG.logD { "readRecords: SFI=$sfi record=$record -> ${recordTlvs.size} TLV(s)" }
                    tlvs.addAll(recordTlvs)
                } else {
                    TAG.logD { "readRecords: SFI=$sfi record=$record -> failed or empty (status=${response.statusWord})" }
                }
            }
        }
        return tlvs
    }

    /**
     * Build minimal PDOL data (all zeros) to satisfy GPO requirements.
     * PDOL is a list of tag+length pairs indicating what terminal data the card expects.
     */
    private fun buildPdolData(pdolBytes: ByteArray): ByteArray {
        var totalLength = 0
        var i = 0
        while (i < pdolBytes.size) {
            // Skip tag bytes
            if (pdolBytes[i].toInt() and 0x1F == 0x1F) {
                i++ // multi-byte tag
                while (i < pdolBytes.size && pdolBytes[i].toInt() and 0x80 != 0) i++
                i++ // last tag byte
            } else {
                i++ // single byte tag
            }
            // Read length
            if (i < pdolBytes.size) {
                totalLength += pdolBytes[i].toInt() and 0xFF
                i++
            }
        }
        return ByteArray(totalLength) // All zeros
    }

    private fun transceive(isoDep: IsoDep, command: ByteArray): ApduResponse {
        TAG.logD { "transceive: >> ${command.toHex()}" }
        var rawResponse = isoDep.transceive(command)
        var response = ApduResponse.from(rawResponse)
        TAG.logD { "transceive: << status=${response.statusWord} (${rawResponse.size} bytes)" }

        // Handle SW_6C (wrong Le) — re-send with correct Le
        if (response.sw1 == 0x6C) {
            TAG.logD { "transceive: SW_6C received, resending with Le=${response.sw2}" }
            val correctedCommand = command.copyOf()
            correctedCommand[correctedCommand.size - 1] = response.sw2.toByte()
            rawResponse = isoDep.transceive(correctedCommand)
            response = ApduResponse.from(rawResponse)
            TAG.logD { "transceive: << (retry) status=${response.statusWord} (${rawResponse.size} bytes)" }
        }

        // Handle SW_61 (more data available) — issue GET RESPONSE
        if (response.sw1 == 0x61) {
            TAG.logD { "transceive: SW_61 received, issuing GET RESPONSE for ${response.sw2} bytes" }
            val getResponse = byteArrayOf(0x00, 0xC0.toByte(), 0x00, 0x00, response.sw2.toByte())
            rawResponse = isoDep.transceive(getResponse)
            response = ApduResponse.from(rawResponse)
            TAG.logD { "transceive: << (GET RESPONSE) status=${response.statusWord} (${rawResponse.size} bytes)" }
        }

        return response
    }
}
