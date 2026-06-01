package com.axondragonscale.cardy.emv.interpreter

import com.axondragonscale.cardy.emv.EmvTags

/** Created by Ronak on 05/30/26 */

/**
 * Interprets EMV tag values at the bit/byte level, similar to
 * paymentcardtools.com/emv-tlv-parser.
 *
 * Given a tag hex and its raw value bytes, produces a [TagInterpretation]
 * explaining what each bit/byte means.
 */
object EmvTagInterpreter {

    /**
     * Interpret the value of a given EMV tag.
     * Returns null if no interpretation is available for this tag.
     */
    fun interpret(tagHex: String, valueBytes: ByteArray): TagInterpretation? {
        if (valueBytes.isEmpty()) return null
        return when (tagHex.uppercase()) {
            "82" -> interpretAip(valueBytes)
            "95" -> interpretTvr(valueBytes)
            "9F07" -> interpretAuc(valueBytes)
            "9F27" -> interpretCid(valueBytes)
            "8E" -> interpretCvmList(valueBytes)
            "94" -> interpretAfl(valueBytes)
            "9F38" -> interpretDol(valueBytes, "PDOL")
            "8C" -> interpretDol(valueBytes, "CDOL1")
            "8D" -> interpretDol(valueBytes, "CDOL2")
            "87" -> interpretAppPriorityIndicator(valueBytes)
            "9C" -> interpretTransactionType(valueBytes)
            "9F33" -> interpretTerminalCapabilities(valueBytes)
            "9F34" -> interpretCvmResults(valueBytes)
            "9F36" -> interpretAtc(valueBytes)
            "5F28" -> interpretCountryCode(valueBytes)
            "5F2A" -> interpretCurrencyCode(valueBytes)
            "9F42" -> interpretCurrencyCode(valueBytes)
            "9F06", "4F", "84" -> interpretAid(valueBytes)
            "9F0D" -> interpretIssuerActionCode(valueBytes, "Default")
            "9F0E" -> interpretIssuerActionCode(valueBytes, "Denial")
            "9F0F" -> interpretIssuerActionCode(valueBytes, "Online")
            "5F24" -> interpretDate(valueBytes, "Expiration")
            "5F25" -> interpretDate(valueBytes, "Effective")
            "9A" -> interpretDate(valueBytes, "Transaction")
            "9F35" -> interpretTerminalType(valueBytes)
            else -> null
        }
    }

    /** Returns true if interpretation is available for this tag */
    fun hasInterpretation(tagHex: String): Boolean = tagHex.uppercase() in setOf(
        "82", "95", "9F07", "9F27", "8E", "94", "9F38", "8C", "8D",
        "87", "9C", "9F33", "9F34", "9F36", "5F28", "5F2A", "9F42",
        "9F06", "4F", "84", "9F0D", "9F0E", "9F0F", "5F24", "5F25",
        "9A", "9F35",
    )

    // region AIP (Tag 82) - Application Interchange Profile

    private fun interpretAip(value: ByteArray): TagInterpretation {
        if (value.size < 2) return TagInterpretation(summary = "Invalid AIP (expected 2 bytes)")

        val byte1 = value[0].toInt() and 0xFF
        val byte2 = value[1].toInt() and 0xFF

        val entries = mutableListOf<InterpretedEntry>()

        // Byte 1
        entries.add(InterpretedEntry.BitField(1, 8, "RFU", byte1 and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(1, 7, "SDA supported", byte1 and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(1, 6, "DDA supported", byte1 and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(1, 5, "Cardholder verification supported", byte1 and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(1, 4, "Terminal risk management to be performed", byte1 and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(1, 3, "Issuer authentication supported", byte1 and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(1, 2, "RFU", byte1 and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(1, 1, "CDA supported", byte1 and 0x01 != 0))

        // Byte 2
        entries.add(InterpretedEntry.BitField(2, 8, "Enciphered PIN for online verification supported (MSD)", byte2 and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(2, 7, "Relay resistance protocol supported", byte2 and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(2, 6, "RFU", byte2 and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(2, 5, "RFU", byte2 and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(2, 4, "RFU", byte2 and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(2, 3, "RFU", byte2 and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(2, 2, "RFU", byte2 and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(2, 1, "RFU", byte2 and 0x01 != 0))

        val capabilities = mutableListOf<String>()
        if (byte1 and 0x40 != 0) capabilities.add("SDA")
        if (byte1 and 0x20 != 0) capabilities.add("DDA")
        if (byte1 and 0x01 != 0) capabilities.add("CDA")
        val summary = if (capabilities.isNotEmpty()) capabilities.joinToString(", ") else "No offline auth"

        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region TVR (Tag 95) - Terminal Verification Results

    private fun interpretTvr(value: ByteArray): TagInterpretation {
        if (value.size < 5) return TagInterpretation(summary = "Invalid TVR (expected 5 bytes)")

        val entries = mutableListOf<InterpretedEntry>()
        val b = IntArray(5) { value[it].toInt() and 0xFF }

        // Byte 1 — Offline data authentication
        entries.add(InterpretedEntry.BitField(1, 8, "Offline data authentication was not performed", b[0] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(1, 7, "SDA failed", b[0] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(1, 6, "ICC data missing", b[0] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(1, 5, "Card appears on terminal exception file", b[0] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(1, 4, "DDA failed", b[0] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(1, 3, "CDA failed", b[0] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(1, 2, "RFU", b[0] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(1, 1, "RFU", b[0] and 0x01 != 0))

        // Byte 2 — Terminal/application
        entries.add(InterpretedEntry.BitField(2, 8, "ICC and terminal have different application versions", b[1] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(2, 7, "Expired application", b[1] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(2, 6, "Application not yet effective", b[1] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(2, 5, "Requested service not allowed for card product", b[1] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(2, 4, "New card", b[1] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(2, 3, "RFU", b[1] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(2, 2, "RFU", b[1] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(2, 1, "RFU", b[1] and 0x01 != 0))

        // Byte 3 — Cardholder verification
        entries.add(InterpretedEntry.BitField(3, 8, "Cardholder verification was not successful", b[2] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(3, 7, "Unrecognised CVM", b[2] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(3, 6, "PIN try limit exceeded", b[2] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(3, 5, "PIN entry required and PIN pad not present or not working", b[2] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(3, 4, "PIN entry required, PIN pad present, but PIN was not entered", b[2] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(3, 3, "Online PIN entered", b[2] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(3, 2, "RFU", b[2] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(3, 1, "RFU", b[2] and 0x01 != 0))

        // Byte 4 — Transaction
        entries.add(InterpretedEntry.BitField(4, 8, "Transaction exceeds floor limit", b[3] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(4, 7, "Lower consecutive offline limit exceeded", b[3] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(4, 6, "Upper consecutive offline limit exceeded", b[3] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(4, 5, "Transaction selected randomly for online processing", b[3] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(4, 4, "Merchant forced transaction online", b[3] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(4, 3, "RFU", b[3] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(4, 2, "RFU", b[3] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(4, 1, "RFU", b[3] and 0x01 != 0))

        // Byte 5 — Default/Issuer/Script
        entries.add(InterpretedEntry.BitField(5, 8, "Default TDOL used", b[4] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(5, 7, "Issuer authentication failed", b[4] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(5, 6, "Script processing failed before final GENERATE AC", b[4] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(5, 5, "Script processing failed after final GENERATE AC", b[4] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(5, 4, "RFU", b[4] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(5, 3, "RFU", b[4] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(5, 2, "RFU", b[4] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(5, 1, "RFU", b[4] and 0x01 != 0))

        val setFlags = entries.filterIsInstance<InterpretedEntry.BitField>().filter { it.isSet && !it.label.startsWith("RFU") }
        val summary = if (setFlags.isEmpty()) "No issues detected" else "${setFlags.size} flag(s) set"

        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region AUC (Tag 9F07) - Application Usage Control

    private fun interpretAuc(value: ByteArray): TagInterpretation {
        if (value.size < 2) return TagInterpretation(summary = "Invalid AUC (expected 2 bytes)")

        val byte1 = value[0].toInt() and 0xFF
        val byte2 = value[1].toInt() and 0xFF
        val entries = mutableListOf<InterpretedEntry>()

        // Byte 1
        entries.add(InterpretedEntry.BitField(1, 8, "Valid for domestic cash transactions", byte1 and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(1, 7, "Valid for international cash transactions", byte1 and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(1, 6, "Valid for domestic goods", byte1 and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(1, 5, "Valid for international goods", byte1 and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(1, 4, "Valid for domestic services", byte1 and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(1, 3, "Valid for international services", byte1 and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(1, 2, "Valid at ATMs", byte1 and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(1, 1, "Valid at terminals other than ATMs", byte1 and 0x01 != 0))

        // Byte 2
        entries.add(InterpretedEntry.BitField(2, 8, "Domestic cashback allowed", byte2 and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(2, 7, "International cashback allowed", byte2 and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(2, 6, "RFU", byte2 and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(2, 5, "RFU", byte2 and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(2, 4, "RFU", byte2 and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(2, 3, "RFU", byte2 and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(2, 2, "RFU", byte2 and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(2, 1, "RFU", byte2 and 0x01 != 0))

        val allowed = entries.filterIsInstance<InterpretedEntry.BitField>()
            .filter { it.isSet && !it.label.startsWith("RFU") }
            .map { it.label.removePrefix("Valid for ").removePrefix("Domestic ").removePrefix("International ") }
        val summary = if (allowed.isNotEmpty()) "${allowed.size} usage(s) allowed" else "No usage allowed"

        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region CID (Tag 9F27) - Cryptogram Information Data

    private fun interpretCid(value: ByteArray): TagInterpretation {
        if (value.isEmpty()) return TagInterpretation(summary = "Empty")

        val byte1 = value[0].toInt() and 0xFF
        val cryptogramType = (byte1 shr 6) and 0x03
        val paymentSystemSpecific = (byte1 shr 4) and 0x03
        val adviceRequired = (byte1 shr 3) and 0x01
        val reason = byte1 and 0x07

        val typeName = when (cryptogramType) {
            0 -> "AAC (Application Authentication Cryptogram — transaction declined)"
            1 -> "TC (Transaction Certificate — transaction approved offline)"
            2 -> "ARQC (Authorization Request Cryptogram — online authorization requested)"
            3 -> "RFU"
            else -> "Unknown"
        }

        val entries = mutableListOf<InterpretedEntry>()
        entries.add(InterpretedEntry.DecodedValue("Cryptogram Type (bits 8-7)", typeName, "%02X".format(cryptogramType)))
        entries.add(InterpretedEntry.DecodedValue("Payment System Specific (bits 6-5)", "%02d".format(paymentSystemSpecific), "%02X".format(paymentSystemSpecific)))
        entries.add(InterpretedEntry.DecodedValue("Advice Required (bit 4)", if (adviceRequired == 1) "Yes" else "No", "%02X".format(adviceRequired)))
        entries.add(InterpretedEntry.DecodedValue("Reason/Qualifier (bits 3-1)", "%03d".format(reason), "%02X".format(reason)))

        val summary = when (cryptogramType) {
            0 -> "AAC (Declined)"
            1 -> "TC (Approved Offline)"
            2 -> "ARQC (Go Online)"
            else -> "Unknown"
        }

        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region CVM List (Tag 8E) - Cardholder Verification Method List

    private fun interpretCvmList(value: ByteArray): TagInterpretation {
        if (value.size < 8) return TagInterpretation(summary = "Invalid CVM List (min 8 bytes)")

        val entries = mutableListOf<InterpretedEntry>()

        // First 4 bytes: Amount field X
        val amountX = value.sliceArray(0..3).toLong()
        entries.add(InterpretedEntry.DecodedValue("Amount X (threshold)", amountX.toString(), value.sliceArray(0..3).toHexString()))

        // Next 4 bytes: Amount field Y
        val amountY = value.sliceArray(4..7).toLong()
        entries.add(InterpretedEntry.DecodedValue("Amount Y (secondary threshold)", amountY.toString(), value.sliceArray(4..7).toHexString()))

        // Remaining bytes: CVM rules (2 bytes each)
        val rulesData = value.sliceArray(8 until value.size)
        val ruleCount = rulesData.size / 2

        for (i in 0 until ruleCount) {
            val ruleByte1 = rulesData[i * 2].toInt() and 0xFF
            val ruleByte2 = rulesData[i * 2 + 1].toInt() and 0xFF

            val failAction = if (ruleByte1 and 0x40 != 0) "Apply next rule" else "Fail CVM processing"
            val methodCode = ruleByte1 and 0x3F
            val methodName = getCvmMethodName(methodCode)
            val conditionName = getCvmConditionName(ruleByte2)

            val ruleEntries = listOf(
                InterpretedEntry.DecodedValue("Method", methodName, "%02X".format(ruleByte1)),
                InterpretedEntry.DecodedValue("Condition", conditionName, "%02X".format(ruleByte2)),
                InterpretedEntry.DecodedValue("If fails", failAction, ""),
            )
            entries.add(InterpretedEntry.StructuredGroup("Rule ${i + 1}", ruleEntries))
        }

        return TagInterpretation(
            summary = "$ruleCount CVM rule(s)",
            entries = entries,
        )
    }

    private fun getCvmMethodName(code: Int): String = when (code) {
        0x00 -> "Fail CVM processing"
        0x01 -> "Plaintext PIN verification performed by ICC"
        0x02 -> "Enciphered PIN verified online"
        0x03 -> "Plaintext PIN verification performed by ICC and signature (paper)"
        0x04 -> "Enciphered PIN verification performed by ICC"
        0x05 -> "Enciphered PIN verification performed by ICC and signature (paper)"
        0x1E -> "Signature (paper)"
        0x1F -> "No CVM required"
        0x20 -> "No CVM required (contactless)"
        in 0x06..0x1D -> "RFU ($%02X)".format(code)
        in 0x21..0x3E -> "RFU ($%02X)".format(code)
        0x3F -> "Not available for use (N/A)"
        else -> "Unknown ($%02X)".format(code)
    }

    private fun getCvmConditionName(code: Int): String = when (code) {
        0x00 -> "Always"
        0x01 -> "If unattended cash"
        0x02 -> "If not (unattended cash nor manual cash nor purchase with cashback)"
        0x03 -> "If terminal supports the CVM"
        0x04 -> "If manual cash"
        0x05 -> "If purchase with cashback"
        0x06 -> "If transaction is in the application currency and under X value"
        0x07 -> "If transaction is in the application currency and over X value"
        0x08 -> "If transaction is in the application currency and under Y value"
        0x09 -> "If transaction is in the application currency and over Y value"
        in 0x0A..0x7F -> "RFU ($%02X)".format(code)
        in 0x80..0xFF -> "Payment system specific ($%02X)".format(code)
        else -> "Unknown ($%02X)".format(code)
    }

    // endregion

    // region AFL (Tag 94) - Application File Locator

    private fun interpretAfl(value: ByteArray): TagInterpretation {
        if (value.size % 4 != 0) return TagInterpretation(summary = "Invalid AFL (must be multiple of 4 bytes)")

        val entries = mutableListOf<InterpretedEntry>()
        val entryCount = value.size / 4

        for (i in 0 until entryCount) {
            val offset = i * 4
            val sfi = (value[offset].toInt() and 0xFF) shr 3
            val firstRecord = value[offset + 1].toInt() and 0xFF
            val lastRecord = value[offset + 2].toInt() and 0xFF
            val odaRecords = value[offset + 3].toInt() and 0xFF

            val aflEntries = listOf(
                InterpretedEntry.DecodedValue("SFI", sfi.toString(), "%02X".format(value[offset].toInt() and 0xFF)),
                InterpretedEntry.DecodedValue("First Record", firstRecord.toString(), "%02X".format(firstRecord)),
                InterpretedEntry.DecodedValue("Last Record", lastRecord.toString(), "%02X".format(lastRecord)),
                InterpretedEntry.DecodedValue("ODA Records", odaRecords.toString(), "%02X".format(odaRecords)),
            )
            entries.add(InterpretedEntry.StructuredGroup("File ${i + 1} (SFI $sfi)", aflEntries))
        }

        return TagInterpretation(
            summary = "$entryCount file(s) to read",
            entries = entries,
        )
    }

    // endregion

    // region DOL (Tags 9F38, 8C, 8D) - Data Object Lists

    @OptIn(ExperimentalStdlibApi::class)
    private fun interpretDol(value: ByteArray, dolName: String): TagInterpretation {
        val entries = mutableListOf<InterpretedEntry>()
        var offset = 0
        var totalRequestedLength = 0

        while (offset < value.size) {
            // Parse tag
            val tagStart = offset
            val firstByte = value[offset].toInt() and 0xFF
            offset++

            if (firstByte and 0x1F == 0x1F) {
                // Multi-byte tag
                while (offset < value.size && (value[offset].toInt() and 0x80) != 0) {
                    offset++
                }
                if (offset < value.size) offset++ // Last byte of multi-byte tag
            }

            val tagBytes = value.sliceArray(tagStart until offset)
            val tagHex = tagBytes.toHexString().uppercase()

            // Parse length (single byte in DOL)
            if (offset >= value.size) break
            val requestedLength = value[offset].toInt() and 0xFF
            offset++
            totalRequestedLength += requestedLength

            val tagName = EmvTags.getTagName(tagHex)
            entries.add(InterpretedEntry.DolEntry(tagHex, tagName, requestedLength))
        }

        return TagInterpretation(
            summary = "${entries.size} tag(s), $totalRequestedLength byte(s) total",
            entries = entries,
        )
    }

    // endregion

    // region Application Priority Indicator (Tag 87)

    private fun interpretAppPriorityIndicator(value: ByteArray): TagInterpretation {
        if (value.isEmpty()) return TagInterpretation(summary = "Empty")

        val byte1 = value[0].toInt() and 0xFF
        val confirmationRequired = byte1 and 0x80 != 0
        val priority = byte1 and 0x0F

        val entries = listOf(
            InterpretedEntry.BitField(1, 8, "Cardholder confirmation required", confirmationRequired),
            InterpretedEntry.DecodedValue("Priority", if (priority == 0) "No priority assigned" else priority.toString(), "%02X".format(priority)),
        )

        val summary = buildString {
            append("Priority: ${if (priority == 0) "None" else priority.toString()}")
            if (confirmationRequired) append(" (confirmation required)")
        }

        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region Transaction Type (Tag 9C)

    private fun interpretTransactionType(value: ByteArray): TagInterpretation {
        if (value.isEmpty()) return TagInterpretation(summary = "Empty")

        val code = value[0].toInt() and 0xFF
        val typeName = when (code) {
            0x00 -> "Purchase (Goods/Services)"
            0x01 -> "Cash Advance"
            0x09 -> "Purchase with Cashback"
            0x20 -> "Refund"
            0x30 -> "Balance Inquiry"
            else -> "Unknown ($%02X)".format(code)
        }

        return TagInterpretation(
            summary = typeName,
            entries = listOf(InterpretedEntry.DecodedValue("Transaction Type", typeName, "%02X".format(code))),
        )
    }

    // endregion

    // region Terminal Capabilities (Tag 9F33)

    private fun interpretTerminalCapabilities(value: ByteArray): TagInterpretation {
        if (value.size < 3) return TagInterpretation(summary = "Invalid (expected 3 bytes)")

        val b = IntArray(3) { value[it].toInt() and 0xFF }
        val entries = mutableListOf<InterpretedEntry>()

        // Byte 1 — Card data input capability
        entries.add(InterpretedEntry.BitField(1, 8, "Manual key entry", b[0] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(1, 7, "Magnetic stripe", b[0] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(1, 6, "IC with contacts", b[0] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(1, 5, "RFU", b[0] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(1, 4, "RFU", b[0] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(1, 3, "RFU", b[0] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(1, 2, "RFU", b[0] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(1, 1, "RFU", b[0] and 0x01 != 0))

        // Byte 2 — CVM capability
        entries.add(InterpretedEntry.BitField(2, 8, "Plaintext PIN for ICC verification", b[1] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(2, 7, "Enciphered PIN for online verification", b[1] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(2, 6, "Signature (paper)", b[1] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(2, 5, "Enciphered PIN for offline verification", b[1] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(2, 4, "No CVM required", b[1] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(2, 3, "RFU", b[1] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(2, 2, "RFU", b[1] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(2, 1, "RFU", b[1] and 0x01 != 0))

        // Byte 3 — Security capability
        entries.add(InterpretedEntry.BitField(3, 8, "SDA", b[2] and 0x80 != 0))
        entries.add(InterpretedEntry.BitField(3, 7, "DDA", b[2] and 0x40 != 0))
        entries.add(InterpretedEntry.BitField(3, 6, "Card capture", b[2] and 0x20 != 0))
        entries.add(InterpretedEntry.BitField(3, 5, "CDA", b[2] and 0x10 != 0))
        entries.add(InterpretedEntry.BitField(3, 4, "RFU", b[2] and 0x08 != 0))
        entries.add(InterpretedEntry.BitField(3, 3, "RFU", b[2] and 0x04 != 0))
        entries.add(InterpretedEntry.BitField(3, 2, "RFU", b[2] and 0x02 != 0))
        entries.add(InterpretedEntry.BitField(3, 1, "RFU", b[2] and 0x01 != 0))

        val inputCaps = mutableListOf<String>()
        if (b[0] and 0x80 != 0) inputCaps.add("Key")
        if (b[0] and 0x40 != 0) inputCaps.add("Mag")
        if (b[0] and 0x20 != 0) inputCaps.add("IC")
        val summary = "Input: ${inputCaps.joinToString("+").ifEmpty { "None" }}"

        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region CVM Results (Tag 9F34)

    private fun interpretCvmResults(value: ByteArray): TagInterpretation {
        if (value.size < 3) return TagInterpretation(summary = "Invalid (expected 3 bytes)")

        val method = value[0].toInt() and 0x3F
        val condition = value[1].toInt() and 0xFF
        val result = value[2].toInt() and 0xFF

        val methodName = getCvmMethodName(method)
        val conditionName = getCvmConditionName(condition)
        val resultName = when (result) {
            0x00 -> "Unknown"
            0x01 -> "Failed"
            0x02 -> "Successful"
            else -> "Unknown ($%02X)".format(result)
        }

        val entries = listOf(
            InterpretedEntry.DecodedValue("CVM Performed", methodName, "%02X".format(value[0].toInt() and 0xFF)),
            InterpretedEntry.DecodedValue("Condition", conditionName, "%02X".format(condition)),
            InterpretedEntry.DecodedValue("Result", resultName, "%02X".format(result)),
        )

        return TagInterpretation(summary = "$resultName — $methodName", entries = entries)
    }

    // endregion

    // region ATC (Tag 9F36) - Application Transaction Counter

    private fun interpretAtc(value: ByteArray): TagInterpretation {
        if (value.isEmpty()) return TagInterpretation(summary = "Empty")

        val counter = value.toLong()
        return TagInterpretation(
            summary = "Count: $counter",
            entries = listOf(
                InterpretedEntry.DecodedValue("Transaction Count", counter.toString(), value.toHexString()),
            ),
        )
    }

    // endregion

    // region Country Code (Tag 5F28)

    private fun interpretCountryCode(value: ByteArray): TagInterpretation {
        // BCD-encoded country code (ISO 3166)
        val codeStr = value.toHexString().trimStart('0')
        val countryName = ISO_COUNTRY_CODES[codeStr] ?: ISO_COUNTRY_CODES[codeStr.padStart(3, '0')]
        val summary = if (countryName != null) "$countryName ($codeStr)" else "Code: $codeStr"
        return TagInterpretation(
            summary = summary,
            entries = listOf(InterpretedEntry.DecodedValue("Country", summary, value.toHexString())),
        )
    }

    // endregion

    // region Currency Code (Tags 5F2A, 9F42)

    private fun interpretCurrencyCode(value: ByteArray): TagInterpretation {
        val codeStr = value.toHexString().trimStart('0')
        val currencyName = ISO_CURRENCY_CODES[codeStr] ?: ISO_CURRENCY_CODES[codeStr.padStart(3, '0')]
            ?: ISO_CURRENCY_CODES[codeStr.padStart(4, '0')]
        val summary = if (currencyName != null) "$currencyName ($codeStr)" else "Code: $codeStr"
        return TagInterpretation(
            summary = summary,
            entries = listOf(InterpretedEntry.DecodedValue("Currency", summary, value.toHexString())),
        )
    }

    // endregion

    // region AID interpretation (Tags 4F, 84, 9F06)

    private fun interpretAid(value: ByteArray): TagInterpretation {
        val hex = value.toHexString().uppercase()
        val ridHex = if (hex.length >= 10) hex.substring(0, 10) else hex
        val network = KNOWN_RIDS[ridHex]
        val summary = network ?: "Unknown network"
        val entries = mutableListOf<InterpretedEntry>()
        entries.add(InterpretedEntry.DecodedValue("RID (Registered App Provider)", network ?: "Unknown", ridHex))
        if (hex.length > 10) {
            entries.add(InterpretedEntry.DecodedValue("PIX (Proprietary App Identifier Extension)", hex.substring(10), hex.substring(10)))
        }
        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region Issuer Action Codes (Tags 9F0D, 9F0E, 9F0F)

    private fun interpretIssuerActionCode(value: ByteArray, type: String): TagInterpretation {
        // Same bit layout as TVR — each bit corresponds to a TVR condition
        if (value.size < 5) return TagInterpretation(summary = "Invalid IAC-$type (expected 5 bytes)")

        val entries = mutableListOf<InterpretedEntry>()
        val b = IntArray(5) { value[it].toInt() and 0xFF }

        // Uses same bit definitions as TVR
        val tvrLabels = listOf(
            // Byte 1
            listOf("Offline data auth not performed", "SDA failed", "ICC data missing", "Card on exception file", "DDA failed", "CDA failed", "RFU", "RFU"),
            // Byte 2
            listOf("Different app versions", "Expired app", "App not yet effective", "Service not allowed", "New card", "RFU", "RFU", "RFU"),
            // Byte 3
            listOf("CVM not successful", "Unrecognised CVM", "PIN try limit exceeded", "PIN pad not present/working", "PIN not entered", "Online PIN entered", "RFU", "RFU"),
            // Byte 4
            listOf("Exceeds floor limit", "Lower offline limit exceeded", "Upper offline limit exceeded", "Random online selection", "Merchant forced online", "RFU", "RFU", "RFU"),
            // Byte 5
            listOf("Default TDOL used", "Issuer auth failed", "Script failed before GAC", "Script failed after GAC", "RFU", "RFU", "RFU", "RFU"),
        )

        for (byteIdx in 0..4) {
            for (bitIdx in 0..7) {
                val mask = 0x80 shr bitIdx
                val isSet = b[byteIdx] and mask != 0
                entries.add(InterpretedEntry.BitField(byteIdx + 1, 8 - bitIdx, tvrLabels[byteIdx][bitIdx], isSet))
            }
        }

        val setCount = entries.filterIsInstance<InterpretedEntry.BitField>().count { it.isSet && !it.label.startsWith("RFU") }
        val summary = "IAC-$type: $setCount condition(s) trigger ${type.lowercase()}"

        return TagInterpretation(summary = summary, entries = entries)
    }

    // endregion

    // region Date interpretation (Tags 5F24, 5F25, 9A)

    private fun interpretDate(value: ByteArray, label: String): TagInterpretation {
        val hex = value.toHexString()
        if (hex.length < 6) return TagInterpretation(summary = "Invalid date")

        val yy = hex.substring(0, 2)
        val mm = hex.substring(2, 4)
        val dd = hex.substring(4, 6)
        val formatted = "20$yy-$mm-$dd"

        return TagInterpretation(
            summary = formatted,
            entries = listOf(
                InterpretedEntry.DecodedValue("$label Date", formatted, hex),
                InterpretedEntry.DecodedValue("Year", "20$yy", yy),
                InterpretedEntry.DecodedValue("Month", mm, mm),
                InterpretedEntry.DecodedValue("Day", dd, dd),
            ),
        )
    }

    // endregion

    // region Terminal Type (Tag 9F35)

    private fun interpretTerminalType(value: ByteArray): TagInterpretation {
        if (value.isEmpty()) return TagInterpretation(summary = "Empty")

        val code = value[0].toInt() and 0xFF
        val environment = when (code / 10) {
            1, 2 -> "Financial Institution"
            3, 4 -> "Merchant"
            else -> "Unknown"
        }
        val attendance = when (code % 10) {
            1, 4 -> "Attended"
            2, 5 -> "Unattended"
            3, 6 -> "No terminal"
            else -> "Unknown"
        }
        val typeName = when (code) {
            11 -> "Financial Institution — Attended — Online only"
            12 -> "Financial Institution — Attended — Offline with online capability"
            13 -> "Financial Institution — Attended — Offline only"
            14 -> "Financial Institution — Unattended — Online only"
            15 -> "Financial Institution — Unattended — Offline with online capability"
            16 -> "Financial Institution — Unattended — Offline only"
            21 -> "Merchant — Attended — Online only"
            22 -> "Merchant — Attended — Offline with online capability"
            23 -> "Merchant — Attended — Offline only"
            24 -> "Merchant — Unattended — Online only"
            25 -> "Merchant — Unattended — Offline with online capability"
            26 -> "Merchant — Unattended — Offline only"
            34 -> "Cardholder — Unattended — Online only"
            35 -> "Cardholder — Unattended — Offline with online capability"
            36 -> "Cardholder — Unattended — Offline only"
            else -> "Unknown type ($%02X)".format(code)
        }

        return TagInterpretation(
            summary = typeName,
            entries = listOf(
                InterpretedEntry.DecodedValue("Terminal Type", typeName, "%02X".format(code)),
                InterpretedEntry.DecodedValue("Environment", environment, ""),
                InterpretedEntry.DecodedValue("Attendance", attendance, ""),
            ),
        )
    }

    // endregion

    // region Helper extensions

    @OptIn(ExperimentalStdlibApi::class)
    private fun ByteArray.toHexString(): String = this.toHexString(HexFormat.UpperCase)

    private fun ByteArray.toLong(): Long {
        var result = 0L
        for (b in this) {
            result = (result shl 8) or (b.toLong() and 0xFF)
        }
        return result
    }

    // endregion

    // region ISO Code Lookups (subset of most common)

    private val KNOWN_RIDS = mapOf(
        "A000000003" to "Visa",
        "A000000004" to "Mastercard",
        "A000000025" to "American Express",
        "A000000065" to "JCB",
        "A000000152" to "Discover",
        "A000000324" to "Discover (DCI)",
        "A000000333" to "UnionPay",
        "A000000029" to "Dankort",
        "A000000042" to "Maestro (France)",
        "D276000025" to "Girocard",
        "A000000098" to "Visa (debit)",
    )

    private val ISO_COUNTRY_CODES = mapOf(
        "036" to "Australia", "040" to "Austria", "056" to "Belgium",
        "076" to "Brazil", "124" to "Canada", "156" to "China",
        "208" to "Denmark", "246" to "Finland", "250" to "France",
        "276" to "Germany", "300" to "Greece", "344" to "Hong Kong",
        "356" to "India", "360" to "Indonesia", "372" to "Ireland",
        "376" to "Israel", "380" to "Italy", "392" to "Japan",
        "410" to "South Korea", "458" to "Malaysia", "484" to "Mexico",
        "528" to "Netherlands", "554" to "New Zealand", "578" to "Norway",
        "608" to "Philippines", "616" to "Poland", "620" to "Portugal",
        "643" to "Russia", "702" to "Singapore", "710" to "South Africa",
        "724" to "Spain", "752" to "Sweden", "756" to "Switzerland",
        "764" to "Thailand", "792" to "Turkey", "826" to "United Kingdom",
        "840" to "United States", "784" to "UAE", "682" to "Saudi Arabia",
    )

    private val ISO_CURRENCY_CODES = mapOf(
        "036" to "AUD", "124" to "CAD", "156" to "CNY", "208" to "DKK",
        "344" to "HKD", "356" to "INR", "392" to "JPY", "410" to "KRW",
        "458" to "MYR", "484" to "MXN", "554" to "NZD", "578" to "NOK",
        "608" to "PHP", "643" to "RUB", "702" to "SGD", "710" to "ZAR",
        "752" to "SEK", "756" to "CHF", "764" to "THB", "826" to "GBP",
        "840" to "USD", "978" to "EUR", "985" to "PLN", "784" to "AED",
        "682" to "SAR", "0840" to "USD", "0978" to "EUR", "0826" to "GBP",
        "0356" to "INR",
    )

    // endregion
}
