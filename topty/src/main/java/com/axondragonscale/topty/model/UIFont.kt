package com.axondragonscale.topty.model

/**
 * Created by Ronak Harkhani on 30/10/24
 */
enum class UIFont(val displayName: String) {
    ChakraPetch("Chakra Petch"),
    Geo("Geo"),
    Oxanium("Oxanium");

    companion object {
        val DEFAULT get() = Oxanium

        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
