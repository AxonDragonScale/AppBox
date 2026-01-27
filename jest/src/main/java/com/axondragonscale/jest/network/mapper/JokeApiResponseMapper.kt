package com.axondragonscale.jest.network.mapper

import com.axondragonscale.appdump.jest.database.entity.JokeEntity
import com.axondragonscale.appdump.jest.network.response.JokeApiResponse

/**
 * Created by Ronak Harkhani on 05/04/24
 */

fun JokeApiResponse.toEntity() = JokeEntity(
    id = id,
    timestamp = System.currentTimeMillis(),
    lang = lang,
    category = category,
    flags = flags,
    safe = safe,
    favorite = false,
    type = type,
    joke = joke,
    setup = setup,
    delivery = delivery,
)
