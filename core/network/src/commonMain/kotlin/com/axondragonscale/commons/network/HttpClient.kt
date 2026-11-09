package com.axondragonscale.commons.network

import io.ktor.client.HttpClient

/**
 * Created by Ronak Harkhani on 27 January 2026.
 *
 * Creates a platform-specific [HttpClient] with common plugins pre-installed.
 * Each platform provides an actual implementation supplying the correct engine.
 */
internal expect fun createPlatformHttpClient(): HttpClient

fun createHttpClient(): HttpClient = createPlatformHttpClient()
