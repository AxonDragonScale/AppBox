package com.axondragonscale.jest

import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.jest.di.JestModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Created by Ronak Harkhani on 27 January 2026.
 *
 * Initializes Koin with all common modules.
 * Each platform passes its own [KoinAppDeclaration] for platform-specific setup
 * (e.g. Android passes androidContext + androidLogger).
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            CommonsModule,
            JestModule,
        )
    }
}
