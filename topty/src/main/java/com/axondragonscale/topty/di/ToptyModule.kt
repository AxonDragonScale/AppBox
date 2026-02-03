package com.axondragonscale.topty.di

import com.axondragonscale.topty.core.TTS
import com.axondragonscale.topty.database.ToptyDatabase
import com.axondragonscale.topty.repository.AppPrefsRepository
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.util.DevTools
import com.axondragonscale.topty.ui.token.TokenVM
import com.axondragonscale.topty.ui.home.HomeVM
import com.axondragonscale.topty.ui.qrscanner.QRScannerVM
import com.axondragonscale.topty.ui.settings.SettingsVM
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 15/09/24
 */

val ToptyModule = module {

    // Database
    singleOf(ToptyDatabase::create)
    singleOf(ToptyDatabase::codeDao)

    // Repository
    singleOf(::TokenRepository)
    singleOf(::AppPrefsRepository)

    // Core
    singleOf(::TTS)

    // ViewModel
    viewModelOf(::HomeVM)
    viewModelOf(::QRScannerVM)
    viewModelOf(::SettingsVM)
    viewModelOf(::TokenVM)

    factoryOf(::DevTools)

}
