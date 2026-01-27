package com.axondragonscale.jest.database.mapper

import com.axondragonscale.jest.database.entity.JokeEntity
import com.axondragonscale.jest.model.IJoke
import com.axondragonscale.jest.model.JokeType
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.model.TwoPartJoke

/**
 * Created by Ronak Harkhani on 04/04/24
 */

fun JokeEntity.toModel(): IJoke = when (type) {
    JokeType.Single -> OnePartJoke(
        id = id,
        lang = lang,
        category = category,
        flags = flags,
        safe = safe,
        favorite = favorite,
        timestamp = timestamp,
        type = type,
        joke = joke!!,
    )
    JokeType.TwoPart -> TwoPartJoke(
        id = id,
        lang = lang,
        category = category,
        flags = flags,
        safe = safe,
        favorite = favorite,
        timestamp = timestamp,
        type = type,
        setup = setup!!,
        delivery = delivery!!,
    )
}
