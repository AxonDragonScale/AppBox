package com.axondragonscale.devinator.di

import com.axondragonscale.devinator.background.DevAlarms
import com.axondragonscale.devinator.permission.GrantRepository
import com.axondragonscale.devinator.repository.AppPrefsRepository
import com.axondragonscale.devinator.repository.DevSettingsRepository
import com.axondragonscale.devinator.repository.DevTweaksRepository
import com.axondragonscale.devinator.repository.QuickSettingsTileRepository
import com.axondragonscale.devinator.repository.SettingsRepository
import com.axondragonscale.devinator.repository.TimedToggleRepository
import com.axondragonscale.devinator.repository.WirelessEndpointRepository
import com.axondragonscale.devinator.ui.home.HomeVM
import com.axondragonscale.devinator.ui.settings.SettingsVM
import com.axondragonscale.devinator.ui.setup.SetupVM
import com.axondragonscale.devinator.ui.tiles.QuickSettingsTilesVM
import com.axondragonscale.devinator.ui.tweaks.TweaksVM
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Created by Ronak on 11/06/26 */
val DevinatorModule = module {
    singleOf(::AppPrefsRepository)
    singleOf(::SettingsRepository)
    singleOf(::WirelessEndpointRepository)
    singleOf(::GrantRepository)
    singleOf(::QuickSettingsTileRepository)
    singleOf(::DevAlarms)
    singleOf(::DevSettingsRepository)
    singleOf(::DevTweaksRepository)
    singleOf(::TimedToggleRepository)

    viewModelOf(::SetupVM)
    viewModelOf(::HomeVM)
    viewModelOf(::SettingsVM)
    viewModelOf(::QuickSettingsTilesVM)
    viewModelOf(::TweaksVM)
}
