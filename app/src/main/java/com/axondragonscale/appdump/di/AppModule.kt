package com.axondragonscale.appdump.di

import com.axondragonscale.appdump.data.AppSettings
import com.axondragonscale.appdump.ui.AppViewModel
import com.axondragonscale.appdump.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AppModule = module {
    singleOf(::AppSettings)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AppViewModel)
}
