package com.axondragonscale.jest.network.response

import com.axondragonscale.jest.model.Category
import com.axondragonscale.jest.model.Flags
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.Language
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





