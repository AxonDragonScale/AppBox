package com.axondragonscale.appdump.jest.network.response

import com.axondragonscale.appdump.jest.model.IJoke
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
