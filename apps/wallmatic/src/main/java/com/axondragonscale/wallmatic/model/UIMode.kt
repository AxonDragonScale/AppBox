package com.axondragonscale.wallmatic.model

/**
 * Created by Ronak Harkhani on 19/06/24
 */
enum class UIMode {
    LIGHT,
    AUTO,
    DARK;

    companion object {
        fun fromOrdinal(ordinal: Int) = UIMode.entries[ordinal]
    }
}
