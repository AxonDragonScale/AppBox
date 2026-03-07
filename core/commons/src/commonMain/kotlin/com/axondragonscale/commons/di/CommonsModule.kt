package com.axondragonscale.commons.di

import com.axondragonscale.commons.CommonJson
import com.axondragonscale.commons.network.createHttpClient
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 07 March 2026.
 */

val CommonsModule = module {
    single { CommonJson }
    single { createHttpClient() }
}