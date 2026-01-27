package com.axondragonscale.jest.network.response

import com.axondragonscale.jest.model.IJoke
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 03/04/24
 */

@Serializable
data class JokeListApiResponse(
    val error: Boolean,
    val amount: Int,
    val jokes: List<IJoke>
)
