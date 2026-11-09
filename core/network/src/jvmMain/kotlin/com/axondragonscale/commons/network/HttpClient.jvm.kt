package com.axondragonscale.commons.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.mp.KoinPlatform

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */

internal actual fun createPlatformHttpClient(): HttpClient =
    HttpClient(CIO) {
        val koin = KoinPlatform.getKoin()

        install(ContentNegotiation) {
            json(koin.get())
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }
    }
