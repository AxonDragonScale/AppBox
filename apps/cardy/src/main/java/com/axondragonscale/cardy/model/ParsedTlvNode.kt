package com.axondragonscale.cardy.model

import com.axondragonscale.cardy.emv.EmvTagInfo
import com.axondragonscale.cardy.emv.interpreter.TagInterpretation

/** Created by Ronak on 05/30/26 */
data class ParsedTlvNode(
    val tagHex: String,
    val tagInfo: EmvTagInfo,
    val valueHex: String,
    val valueAscii: String?,
    val length: Int,
    val isConstructed: Boolean,
    val children: List<ParsedTlvNode>,
    val interpretation: TagInterpretation? = null,
)
