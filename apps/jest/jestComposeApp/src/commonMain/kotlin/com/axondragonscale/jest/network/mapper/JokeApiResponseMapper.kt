package com.axondragonscale.jest.network.mapper

import com.axondragonscale.jest.database.entity.JokeEntity
import com.axondragonscale.jest.network.response.JokeApiResponse
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Created by Ronak Harkhani on 05/04/24
 */

@OptIn(ExperimentalTime::class)
fun JokeApiResponse.toEntity() = JokeEntity(
    id = id,
    timestamp = Clock.System.now().toEpochMilliseconds(),
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
