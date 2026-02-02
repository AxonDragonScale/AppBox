package com.axondragonscale.appbox.di

import com.axondragonscale.appbox.data.AppSettings
import com.axondragonscale.appbox.ui.AppViewModel
import com.axondragonscale.appbox.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AppModule = module {
    singleOf(::AppSettings)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AppViewModel)
}
