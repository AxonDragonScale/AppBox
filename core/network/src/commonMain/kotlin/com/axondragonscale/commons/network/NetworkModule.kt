package com.axondragonscale.commons.network

import com.axondragonscale.commons.CommonJson
import org.koin.dsl.module

/**
 * Koin bindings for networking. Include this instead of the old `CommonsModule` when an app
 * actually makes network calls.
 */
val NetworkModule = module {
    single { CommonJson }
    single { createHttpClient() }
}
