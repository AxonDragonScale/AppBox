/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.util

import com.axondragonscale.cardy.model.CardReadResult
import com.axondragonscale.cardy.model.EmvApplication
import com.axondragonscale.cardy.repository.SerializableApplication
import com.axondragonscale.cardy.repository.SerializableCardData

fun CardReadResult.toSerializable() = SerializableCardData(
    applications = applications.map { it.toSerializable() },
    primaryNetwork = primaryNetwork.name,
    primaryPan = primaryPan,
    primaryExpiry = primaryExpiry,
    cardholderName = cardholderName,
    timestamp = timestamp,
)

private fun EmvApplication.toSerializable() = SerializableApplication(
    aid = aid,
    network = network.name,
    label = label,
    pan = pan,
    expiry = expiry,
    cardholderName = cardholderName,
    aip = aip,
    afl = afl,
    cvmList = cvmList,
    cdol1 = cdol1,
    cdol2 = cdol2,
    appPriorityIndicator = appPriorityIndicator,
    appEffectiveDate = appEffectiveDate,
    appExpiryDate = appExpiryDate,
    rawTlvHex = rawTlvs.joinToString("") { it.hex },
)
