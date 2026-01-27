package com.axondragonscale.jest.network.response

import com.axondragonscale.appdump.jest.model.Category
import com.axondragonscale.appdump.jest.model.Flags
import com.axondragonscale.appdump.jest.model.JokeType
import com.axondragonscale.appdump.jest.model.Language
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 02/04/24
 */

@Serializable
data class JokeApiResponse(
    val error: Boolean,
    val id: Int,
    val lang: Language,
    val category: Category,
    val flags: Flags,
    val safe: Boolean,
    val type: JokeType,
    val joke: String? = null,
    val setup: String? = null,
    val delivery: String? = null,
)





