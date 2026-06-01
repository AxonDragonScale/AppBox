package com.axondragonscale.cardy.emv

/** Created by Ronak on 05/30/26 */
enum class CardNetwork(val displayName: String, val aidPrefixes: List<String>) {
    VISA("Visa", listOf("A000000003")),
    MASTERCARD("Mastercard", listOf("A000000004")),
    RUPAY("Rupay", listOf("A000000524")),
    AMEX("American Express", listOf("A000000025")),
    DISCOVER("Discover", listOf("A000000152", "A000000324")),
    JCB("JCB", listOf("A000000065")),
    UNIONPAY("UnionPay", listOf("A000000333")),
    UNKNOWN("Unknown", emptyList());

    companion object {
        fun fromAid(aidHex: String): CardNetwork {
            val upperAid = aidHex.uppercase()
            return entries.firstOrNull { network ->
                network.aidPrefixes.any { prefix -> upperAid.startsWith(prefix) }
            } ?: UNKNOWN
        }
    }
}
