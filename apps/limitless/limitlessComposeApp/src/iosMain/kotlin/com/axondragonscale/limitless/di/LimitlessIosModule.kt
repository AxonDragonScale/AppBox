package com.axondragonscale.limitless.di

import com.axondragonscale.limitless.core.ReminderScheduler
import org.koin.dsl.module

val LimitlessIosModule = module {
    single { ReminderScheduler() }
}
