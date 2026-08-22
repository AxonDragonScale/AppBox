package com.axondragonscale.cardy.emv

/** Created by Ronak on 05/30/26 */
enum class CardNetwork(val displayName: String, val aids: List<String>) {
    VISA("Visa", listOf("A0000000031010", "A0000000032010")),
    MASTERCARD("Mastercard", listOf("A0000000041010")),
    MAESTRO("Maestro", listOf("A0000000043060")),
    RUPAY("Rupay", listOf("A0000005241010")),
    AMEX("American Express", listOf("A00000002501")),
    DISCOVER("Discover", listOf("A0000001523010", "A0000003241010")),
    JCB("JCB", listOf("A0000000651010")),
    UNIONPAY("UnionPay", listOf("A000000333010101")),
    UNKNOWN("Unknown", emptyList());

    companion object {
        fun fromAid(aidHex: String): CardNetwork {
            val upperAid = aidHex.uppercase()
            return entries.firstOrNull { network -> network.aids.any(upperAid::startsWith) }
                ?: entries.firstOrNull { network ->
                    network.aids.any { upperAid.startsWith(it.take(10)) }
                }
                ?: UNKNOWN
        }

        fun fromName(name: String): CardNetwork =
            entries.firstOrNull { it.name == name } ?: UNKNOWN
    }
}
