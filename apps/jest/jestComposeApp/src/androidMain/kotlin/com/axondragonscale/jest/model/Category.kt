package com.axondragonscale.jest.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 03/04/24
 */
@Serializable
enum class Category {
    @SerialName("Programming") Programming,
    @SerialName("Misc") Miscellaneous,
    @SerialName("Dark") Dark,
    @SerialName("Pun") Pun,
    @SerialName("Spooky") Spooky,
    @SerialName("Christmas") Christmas;

    companion object {
        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
