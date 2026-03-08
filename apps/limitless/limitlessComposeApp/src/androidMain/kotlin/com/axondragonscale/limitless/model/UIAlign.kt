package com.axondragonscale.limitless.model

/**
 * Created by Ronak Harkhani on 03/11/24
 */
enum class UIAlign {
    Top,
    Bottom;

    companion object {
        val DEFAULT get() = Bottom

        fun fromOrdinal(ordinal: Int) = UIAlign.entries[ordinal]
    }

}
