package com.axondragonscale.jest.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 03/04/24
 */
@Serializable
enum class BlacklistFlag {
    @SerialName("nsfw") NSFW,
    @SerialName("religious") Religious,
    @SerialName("political") Political,
    @SerialName("racist") Racist,
    @SerialName("sexist") Sexist,
    @SerialName("explicit") Explicit;

    companion object {
        fun fromOrdinal(ordinal: Int) = BlacklistFlag.entries[ordinal]
    }

}
