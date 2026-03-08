package com.axondragonscale.limitless.di

import com.axondragonscale.limitless.core.ReminderNotificationManager
import com.axondragonscale.limitless.core.ReminderScheduler
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Android-specific Koin bindings that cannot live in [LimitlessModule] (commonMain)
 * because they depend on Android-only classes.
 */
val LimitlessAndroidModule = module {
    singleOf(::ReminderScheduler)
    singleOf(::ReminderNotificationManager)
}
