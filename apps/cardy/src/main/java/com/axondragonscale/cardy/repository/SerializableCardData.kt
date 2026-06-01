/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.repository

import kotlinx.serialization.Serializable

@Serializable
data class SerializableCardData(
    val applications: List<SerializableApplication>,
    val primaryNetwork: String,
    val primaryPan: String?,
    val primaryExpiry: String?,
    val cardholderName: String?,
    val timestamp: Long,
)

@Serializable
data class SerializableApplication(
    val aid: String,
    val network: String,
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
    val rawTlvHex: String,
)
