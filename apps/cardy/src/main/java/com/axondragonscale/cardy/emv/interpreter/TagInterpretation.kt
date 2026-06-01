package com.axondragonscale.cardy.emv.interpreter

/** Created by Ronak on 05/30/26 */

/**
 * Represents the decoded/interpreted meaning of an EMV tag's value.
 * Similar to what paymentcardtools.com/emv-tlv-parser shows for each tag.
 */
data class TagInterpretation(
    val summary: String? = null,
    val entries: List<InterpretedEntry> = emptyList(),
)

/**
 * A single interpreted entry from a tag value.
 */
sealed interface InterpretedEntry {

    /**
     * A single bit within a byte that has a specific meaning.
     * e.g., AIP byte 1, bit 7 = "SDA supported"
     */
    data class BitField(
        val byteNumber: Int,
        val bitNumber: Int,
        val label: String,
        val isSet: Boolean,
    ) : InterpretedEntry

    /**
     * A byte or group of bytes decoded to a specific meaning.
     * e.g., Transaction Type: "00" = "Purchase"
     */
    data class DecodedValue(
        val label: String,
        val value: String,
        val rawHex: String,
    ) : InterpretedEntry

    /**
     * A structured group of related fields.
     * e.g., One CVM rule or one AFL entry.
     */
    data class StructuredGroup(
        val label: String,
        val entries: List<InterpretedEntry>,
    ) : InterpretedEntry

    /**
     * A DOL (Data Object List) entry — tag + requested length.
     * Used for PDOL, CDOL1, CDOL2.
     */
    data class DolEntry(
        val tagHex: String,
        val tagName: String,
        val requestedLength: Int,
    ) : InterpretedEntry
}
