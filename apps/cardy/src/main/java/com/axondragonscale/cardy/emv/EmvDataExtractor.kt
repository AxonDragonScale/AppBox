package com.axondragonscale.cardy.emv

import TLV
import com.axondragonscale.cardy.model.EmvApplication
import com.axondragonscale.cardy.emv.interpreter.EmvTagInterpreter
import com.axondragonscale.cardy.model.ParsedTlvNode
import com.axondragonscale.cardy.util.toHex
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE

/** Created by Ronak on 05/30/26 */
private const val TAG = "EmvDataExtractor"

object EmvDataExtractor {

    fun parseTlvData(data: ByteArray): List<TLV> {
        TAG.logD { "parseTlvData: parsing ${data.size} bytes" }
        val result = mutableListOf<TLV>()
        var offset = 0
        while (offset < data.size) {
            if (data[offset] == 0x00.toByte() || data[offset] == 0xFF.toByte()) {
                offset++
                continue
            }
            try {
                val tlv = TLV.from(data.copyOfRange(offset, data.size))
                result.add(tlv)
                offset += tlv.size
            } catch (e: Exception) {
                TAG.logE { "parseTlvData: parse error at offset=$offset: ${e.message}" }
                break
            }
        }
        TAG.logD { "parseTlvData: parsed ${result.size} TLV(s) from ${data.size} bytes" }
        return result
    }

    fun extractAidsFromPpse(responseData: ByteArray): List<ByteArray> {
        val aids = flatten(parseTlvData(responseData))
            .filter { it.tag.asHex.equals(EmvTag.AID.hex, ignoreCase = true) }
            .map { it.value.valueBytes }
            .toList()
        TAG.logD { "extractAidsFromPpse: found ${aids.size} AID(s): ${aids.joinToString { it.toHex() }}" }
        return aids
    }

    private fun flatten(tlvs: List<TLV>): Sequence<TLV> = sequence {
        for (tlv in tlvs) {
            yield(tlv)
            if (tlv.isNested) {
                yieldAll(flatten(parseTlvData(tlv.value.valueBytes)))
            }
        }
    }

    fun findTag(tlvs: List<TLV>, tag: EmvTag): TLV? =
        flatten(tlvs).firstOrNull { it.tag.asHex.equals(tag.hex, ignoreCase = true) }

    /** Extract structured application data from all TLVs collected during a read */
    fun extractApplicationData(allTlvs: List<TLV>, aidBytes: ByteArray): EmvApplication {
        val aidHex = aidBytes.toHex()
        val network = CardNetwork.fromAid(aidHex)
        val tags = buildMap {
            flatten(allTlvs).forEach { tlv ->
                EmvTag.fromHex(tlv.tag.asHex)?.let { putIfAbsent(it, tlv) }
            }
        }
        fun value(tag: EmvTag): ByteArray? = tags[tag]?.value?.valueBytes

        TAG.logD { "extractApplicationData: AID=$aidHex network=$network" }

        val label = value(EmvTag.APPLICATION_LABEL)
            ?.toString(Charsets.US_ASCII)?.trim()

        // Try tag 5A first, then fall back to Track 2 (tag 57)
        var pan = value(EmvTag.APPLICATION_PAN)?.toHex()
            ?.trimEnd('F', 'f')
        var expiryRaw = value(EmvTag.APPLICATION_EXPIRATION_DATE)?.toHex()

        // Track 2 fallback: format is PAN + 'D' + YYMM + service code + ...
        if (pan == null || expiryRaw == null) {
            val track2 = value(EmvTag.TRACK_2_EQUIVALENT_DATA)?.let(::parseTrack2EquivalentData)
            if (track2 != null) {
                TAG.logD { "extractApplicationData: using Track 2 fallback" }
                if (pan == null) {
                    pan = track2.pan
                    TAG.logD { "extractApplicationData: PAN from Track 2" }
                }
                if (expiryRaw == null) {
                    expiryRaw = track2.expirationDate
                    TAG.logD { "extractApplicationData: expiry from Track 2: $expiryRaw" }
                }
            }
        }

        val expiry = expiryRaw?.let { formatExpiry(it) }

        val cardholderName = value(EmvTag.CARDHOLDER_NAME)
            ?.toString(Charsets.US_ASCII)?.trim()?.replace("/", " ")

        val maskedPan = pan?.let { p ->
            if (p.length >= 4) "*".repeat(p.length - 4) + p.takeLast(4) else "****"
        }
        TAG.logD { "extractApplicationData: label=$label pan=$maskedPan expiry=$expiry cardholderName=$cardholderName" }

        return EmvApplication(
            aid = aidHex,
            network = network,
            label = label,
            pan = pan,
            expiry = expiry,
            cardholderName = cardholderName,
            aip = value(EmvTag.AIP)?.toHex(),
            afl = value(EmvTag.AFL)?.toHex(),
            cvmList = value(EmvTag.CVM_LIST)?.toHex(),
            cdol1 = value(EmvTag.CDOL1)?.toHex(),
            cdol2 = value(EmvTag.CDOL2)?.toHex(),
            appPriorityIndicator = value(EmvTag.APPLICATION_PRIORITY_INDICATOR)?.toHex(),
            appEffectiveDate = value(EmvTag.APPLICATION_EFFECTIVE_DATE)?.toHex(),
            appExpiryDate = value(EmvTag.APPLICATION_EXPIRATION_DATE)?.toHex(),
            rawTlvs = allTlvs,
        )
    }

    fun buildTlvTree(
        tlvs: List<TLV>,
        network: CardNetwork = CardNetwork.UNKNOWN,
    ): List<ParsedTlvNode> = tlvs.map { tlv ->
        val tag = EmvTag.resolve(tlv.tag.asHex)
        val children = if (tlv.isNested) {
            buildTlvTree(parseTlvData(tlv.value.valueBytes), network)
        } else {
            emptyList()
        }
        val interpretation = if (!tlv.isNested) {
            EmvTagInterpreter.interpret(tag, tlv.value.valueBytes, network)
        } else {
            null
        }
        ParsedTlvNode(
            tag = tag,
            valueHex = tlv.value.asHex.uppercase(),
            valueAscii = tlv.value.valueBytes.decodeToAsciiOrNull(),
            length = tlv.length.lengthAsInt,
            isConstructed = tlv.isNested,
            children = children,
            interpretation = interpretation,
        )
    }

    private fun formatExpiry(raw: String): String? {
        if (raw.length < 4) return null
        val yy = raw.substring(0, 2)
        val mm = raw.substring(2, 4)
        return "$mm/$yy"
    }

    private fun ByteArray.decodeToAsciiOrNull(): String? {
        if (isEmpty()) return null
        val decoded = map { it.toInt().and(0xFF).toChar() }
        return if (decoded.all { it in ' '..'~' }) String(decoded.toCharArray()) else null
    }
}
