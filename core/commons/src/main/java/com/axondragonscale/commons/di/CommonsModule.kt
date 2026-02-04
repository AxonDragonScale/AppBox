package com.axondragonscale.commons.di

import com.axondragonscale.commons.createJson
import com.axondragonscale.commons.network.createHttpClient
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */

val CommonsModule = module {
    single { createJson() }
    single { createHttpClient(get(), get()) }
}