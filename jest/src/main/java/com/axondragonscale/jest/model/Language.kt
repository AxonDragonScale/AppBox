package com.axondragonscale.jest.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 03/04/24
 */
@Serializable
enum class Language(val code: String) {
    @SerialName("en") English("en"),
    @SerialName("cs") Czech("cs"),
    @SerialName("de") German("de"),
    @SerialName("es") Spanish("es"),
    @SerialName("fr") French("fr"),
    @SerialName("pt") Portuguese("pt");

    companion object {
        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
