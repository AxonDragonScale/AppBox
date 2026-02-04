package com.axondragonscale.topty.model

/**
 * Created by Ronak Harkhani on 30/10/24
 */
enum class CodeDesign {
    A,
    B,
    C;

    companion object {
        val DEFAULT get() = A

        fun fromOrdinal(ordinal: Int) = CodeDesign.entries[ordinal]
    }
}
