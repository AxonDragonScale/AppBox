package com.axondragonscale.cardy.nfc

import TLV
import android.nfc.tech.IsoDep
import com.axondragonscale.cardy.emv.CardNetwork
import com.axondragonscale.cardy.emv.EmvDataExtractor
import com.axondragonscale.cardy.emv.EmvTag
import com.axondragonscale.cardy.model.CardReadResult
import com.axondragonscale.cardy.model.EmvApplication
import com.axondragonscale.cardy.util.hexToBytes
import com.axondragonscale.cardy.util.toHex
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Created by Ronak on 05/30/26 */
private const val TAG = "CardReader"

class CardReader {

    private val knownAids = CardNetwork.entries
        .flatMap(CardNetwork::aids)
        .map(String::hexToBytes)

    fun readCard(isoDep: IsoDep): Result<CardReadResult> = runCatching {
        TAG.logD { "readCard: starting card read" }
        isoDep.connect()
        isoDep.timeout = 5000

        try {
            val applications = mutableListOf<EmvApplication>()

            // Discover applications through PPSE, then probe common AIDs for cards
            // that do not expose a contactless directory.
            val aids = discoverAids(isoDep)
            if (aids.isEmpty()) {
                TAG.logE { "readCard: no supported payment applications found" }
                throw CardReadException.UnsupportedCard
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
                throw CardReadException.UnreadableCard
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

    private fun discoverAids(isoDep: IsoDep): List<ByteArray> {
        TAG.logD { "discoverAids: selecting PPSE" }
        val ppseResponse = transceive(isoDep, EmvCommands.selectPpse())
        if (ppseResponse.isSuccess) {
            val discovered = EmvDataExtractor.extractAidsFromPpse(ppseResponse.data)
            if (discovered.isNotEmpty()) return discovered
        }

        TAG.logD { "discoverAids: PPSE unavailable or empty; probing known AIDs" }
        return knownAids.filter { aid ->
            transceive(isoDep, EmvCommands.selectAid(aid)).isSuccess
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
        val pdol = EmvDataExtractor.findTag(selectTlvs, EmvTag.PDOL)
        val pdolData = pdol?.let { buildPdolData(it.value.valueBytes) }
        TAG.logD { "readApplication: PDOL ${if (pdol != null) "found (${pdolData?.size} bytes)" else "not found, using empty GPO"}" }

        var gpoResponse = transceive(isoDep, EmvCommands.getProcessingOptions(pdolData))
        if (pdol == null && !gpoResponse.isSuccess && gpoResponse.statusWord == "6985") {
            // Some Mastercard applications omit 9F38 but still reject an empty 83 template.
            TAG.logD { "readApplication: empty GPO rejected; retrying with two-byte zero PDOL" }
            gpoResponse = transceive(
                isoDep,
                EmvCommands.getProcessingOptions(ByteArray(2)),
            )
        }

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
            val recordTlvs = scanCommonRecords(isoDep)
            TAG.logD { "readApplication: fallback record scan found ${recordTlvs.size} TLV(s)" }
            allTlvs.addAll(recordTlvs)
        }

        return EmvDataExtractor.extractApplicationData(allTlvs, aid)
    }

    private fun extractAfl(gpoTlvs: List<TLV>, rawData: ByteArray): ByteArray? {
        // Format 2 (tag 77): AFL is in tag 94
        val aflTag = EmvDataExtractor.findTag(gpoTlvs, EmvTag.AFL)
        if (aflTag != null) {
            TAG.logD { "extractAfl: Format 2 (tag 94) AFL found, ${aflTag.value.valueBytes.size} bytes" }
            return aflTag.value.valueBytes
        }

        // Format 1 (tag 80): AIP (2 bytes) + AFL (rest)
        val format1 = EmvDataExtractor.findTag(gpoTlvs, EmvTag.RESPONSE_MESSAGE_TEMPLATE_1)
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
     * Read-only fallback for applications that expose records but do not return an AFL.
     * Stops each SFI at its first missing record to keep the scan fast enough for NFC.
     */
    private fun scanCommonRecords(isoDep: IsoDep): List<TLV> {
        val tlvs = mutableListOf<TLV>()
        for (sfi in 1..10) {
            for (record in 1..16) {
                val response = transceive(isoDep, EmvCommands.readRecord(record, sfi))
                if (!response.isSuccess || response.data.isEmpty()) break

                val recordTlvs = EmvDataExtractor.parseTlvData(response.data)
                TAG.logD { "scanCommonRecords: SFI=$sfi record=$record -> ${recordTlvs.size} TLV(s)" }
                tlvs.addAll(recordTlvs)
            }
        }
        return tlvs
    }

    /** Build minimal, read-only terminal data for the tags requested by the card. */
    private fun buildPdolData(pdolBytes: ByteArray): ByteArray {
        val result = mutableListOf<Byte>()
        var i = 0
        while (i < pdolBytes.size) {
            val tagStart = i
            val firstTagByte = pdolBytes[i++].toInt() and 0xFF
            if (firstTagByte and 0x1F == 0x1F) {
                while (i < pdolBytes.size) {
                    val tagByte = pdolBytes[i++].toInt() and 0xFF
                    if (tagByte and 0x80 == 0) break
                }
            }
            if (i >= pdolBytes.size) break

            val tag = EmvTag.resolve(pdolBytes.copyOfRange(tagStart, i).toHex())
            val length = pdolBytes[i++].toInt() and 0xFF
            val value = when (tag) {
                EmvTag.TERMINAL_TRANSACTION_QUALIFIERS -> "26000000".hexToBytes()
                EmvTag.AMOUNT_AUTHORIZED -> "000000000001".hexToBytes()
                EmvTag.TRANSACTION_DATE -> LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")).hexToBytes()
                EmvTag.TRANSACTION_TYPE -> byteArrayOf(0x00)
                EmvTag.UNPREDICTABLE_NUMBER -> ByteArray(length).also(SecureRandom()::nextBytes)
                else -> ByteArray(length)
            }
            repeat(length) { index ->
                result += value.getOrElse(index) { 0 }
            }
        }
        return result.toByteArray()
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

sealed class CardReadException : Exception() {
    data object UnsupportedCard : CardReadException()
    data object UnreadableCard : CardReadException()
}
