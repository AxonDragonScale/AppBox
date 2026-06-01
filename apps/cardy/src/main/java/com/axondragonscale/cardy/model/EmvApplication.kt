package com.axondragonscale.cardy.model

import TLV
import com.axondragonscale.cardy.emv.CardNetwork

/** Created by Ronak on 05/30/26 */
data class EmvApplication(
    val aid: String,
    val network: CardNetwork,
    val label: String?,
    val pan: String?,
    val expiry: String?,
    val cardholderName: String?,
    val aip: String?,
    val afl: String?,
    val cvmList: String?,
    val cdol1: String?,
    val cdol2: String?,
    val appPriorityIndicator: String? = null,
    val appEffectiveDate: String? = null,
    val appExpiryDate: String? = null,
    val rawTlvs: List<TLV>,
)
