package com.axondragonscale.cardy.model

import com.axondragonscale.cardy.emv.CardNetwork

/** Created by Ronak on 05/30/26 */
data class CardReadResult(
    val applications: List<EmvApplication>,
    val primaryNetwork: CardNetwork,
    val primaryPan: String?,
    val primaryExpiry: String?,
    val cardholderName: String?,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val displayLabel: String
        get() {
            val network = primaryNetwork.displayName
            val last4 = primaryPan?.takeLast(4) ?: "????"
            return "$network - $last4"
        }
}
