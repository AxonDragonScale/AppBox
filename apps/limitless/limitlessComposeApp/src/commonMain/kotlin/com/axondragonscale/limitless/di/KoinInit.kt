package com.axondragonscale.limitless.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Initializes Koin with all common modules.
 * Each platform passes its own [KoinAppDeclaration] for platform-specific setup
 * (e.g. Android passes androidContext + androidLogger) and an optional [platformModule]
 * for platform-specific bindings (e.g. ReminderScheduler actual constructor args).
 */
fun initKoin(platformModule: Module? = null, config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        val modules = buildList {
            add(LimitlessModule)
            if (platformModule != null) add(platformModule)
        }
        modules(modules)
    }
}
