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
        val aids = mutableListOf<ByteArray>()
        val tlvs = parseTlvData(responseData)
        extractAidsRecursive(tlvs, aids)
        TAG.logD { "extractAidsFromPpse: found ${aids.size} AID(s): ${aids.joinToString { it.toHex() }}" }
        return aids
    }

    private fun extractAidsRecursive(tlvs: List<TLV>, aids: MutableList<ByteArray>) {
        for (tlv in tlvs) {
            if (tlv.tag.asHex.uppercase() == "4F") {
                aids.add(tlv.value.valueBytes)
            }
            if (tlv.isNested) {
                try {
                    val nested = parseTlvData(tlv.value.valueBytes)
                    extractAidsRecursive(nested, aids)
                } catch (_: Exception) {}
            }
        }
    }

    fun findTag(tlvs: List<TLV>, tagHex: String): TLV? {
        val upperTag = tagHex.uppercase()
        for (tlv in tlvs) {
            if (tlv.tag.asHex.uppercase() == upperTag) return tlv
            if (tlv.isNested) {
                try {
                    val nested = parseTlvData(tlv.value.valueBytes)
                    val found = findTag(nested, tagHex)
                    if (found != null) return found
                } catch (_: Exception) {}
            }
        }
        return null
    }

    fun findAllTags(tlvs: List<TLV>, tagHex: String): List<TLV> {
        val results = mutableListOf<TLV>()
        val upperTag = tagHex.uppercase()
        for (tlv in tlvs) {
            if (tlv.tag.asHex.uppercase() == upperTag) results.add(tlv)
            if (tlv.isNested) {
                try {
                    val nested = parseTlvData(tlv.value.valueBytes)
                    results.addAll(findAllTags(nested, tagHex))
                } catch (_: Exception) {}
            }
        }
        return results
    }

    /** Extract structured application data from all TLVs collected during a read */
    fun extractApplicationData(allTlvs: List<TLV>, aidBytes: ByteArray): EmvApplication {
        val aidHex = aidBytes.toHex()
        val network = CardNetwork.fromAid(aidHex)
        TAG.logD { "extractApplicationData: AID=$aidHex network=$network" }

        val label = findTag(allTlvs, "50")?.value?.valueBytes
            ?.toString(Charsets.US_ASCII)?.trim()

        // Try tag 5A first, then fall back to Track 2 (tag 57)
        var pan = findTag(allTlvs, "5A")?.value?.valueBytes?.toHex()
            ?.trimEnd('F', 'f')
        var expiryRaw = findTag(allTlvs, "5F24")?.value?.valueBytes?.toHex()

        // Track 2 fallback: format is PAN + 'D' + YYMM + service code + ...
        if (pan == null || expiryRaw == null) {
            val track2 = findTag(allTlvs, "57")?.value?.valueBytes?.toHex()
                ?.uppercase()?.trimEnd('F')
            if (track2 != null) {
                TAG.logD { "extractApplicationData: using Track 2 fallback" }
                val separatorIndex = track2.indexOf('D')
                if (separatorIndex > 0) {
                    if (pan == null) {
                        pan = track2.substring(0, separatorIndex)
                        TAG.logD { "extractApplicationData: PAN from Track 2" }
                    }
                    if (expiryRaw == null && separatorIndex + 4 < track2.length) {
                        expiryRaw = track2.substring(separatorIndex + 1, separatorIndex + 5)
                        TAG.logD { "extractApplicationData: expiry from Track 2: $expiryRaw" }
                    }
                }
            }
        }

        val expiry = expiryRaw?.let { formatExpiry(it) }

        val cardholderName = findTag(allTlvs, "5F20")?.value?.valueBytes
            ?.toString(Charsets.US_ASCII)?.trim()?.replace("/", " ")

        val maskedPan = pan?.let { p ->
            if (p.length >= 4) "*".repeat(p.length - 4) + p.takeLast(4) else "****"
        }
        TAG.logD { "extractApplicationData: label=$label pan=$maskedPan expiry=$expiry cardholderName=$cardholderName" }

        val aip = findTag(allTlvs, "82")?.value?.valueBytes
        val afl = findTag(allTlvs, "94")?.value?.valueBytes
        val cvmList = findTag(allTlvs, "8E")?.value?.valueBytes
        val cdol1 = findTag(allTlvs, "8C")?.value?.valueBytes
        val cdol2 = findTag(allTlvs, "8D")?.value?.valueBytes
        val appPriorityIndicator = findTag(allTlvs, "87")?.value?.valueBytes
        val appEffectiveDate = findTag(allTlvs, "5F25")?.value?.valueBytes
        val appExpiryDate = findTag(allTlvs, "5F24")?.value?.valueBytes

        return EmvApplication(
            aid = aidHex,
            network = network,
            label = label,
            pan = pan,
            expiry = expiry,
            cardholderName = cardholderName,
            aip = aip?.toHex(),
            afl = afl?.toHex(),
            cvmList = cvmList?.toHex(),
            cdol1 = cdol1?.toHex(),
            cdol2 = cdol2?.toHex(),
            appPriorityIndicator = appPriorityIndicator?.toHex(),
            appEffectiveDate = appEffectiveDate?.toHex(),
            appExpiryDate = appExpiryDate?.toHex(),
            rawTlvs = allTlvs,
        )
    }

    fun buildTlvTree(
        tlvs: List<TLV>,
        network: CardNetwork = CardNetwork.UNKNOWN,
    ): List<ParsedTlvNode> = tlvs.map { tlv ->
        val tagHex = tlv.tag.asHex.uppercase()
        val tagInfo = EmvTags.getTagInfo(tagHex)
        val children = if (tlv.isNested) {
            try {
                buildTlvTree(parseTlvData(tlv.value.valueBytes), network)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        val interpretation = if (!tlv.isNested) {
            EmvTagInterpreter.interpret(tagHex, tlv.value.valueBytes, network)
        } else {
            null
        }
        ParsedTlvNode(
            tagHex = tagHex,
            tagName = tagInfo?.name ?: "Unknown",
            tagDescription = tagInfo?.description,
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
