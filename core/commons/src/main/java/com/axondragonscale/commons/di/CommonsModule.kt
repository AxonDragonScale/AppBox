package com.axondragonscale.commons.di

import com.axondragonscale.commons.CommonJson
import com.axondragonscale.commons.network.createHttpClient
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */

val CommonsModule = module {
    single { CommonJson }
    single { createHttpClient(get(), get()) }
}