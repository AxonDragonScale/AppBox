package com.axondragonscale.commons

import kotlinx.serialization.json.Json

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */

val CommonJson = createJson()

internal fun createJson() = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}