package com.axondragonscale.limitless.di

import com.axondragonscale.limitless.database.LimitlessDatabase
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.devtools.SampleDataSeeder
import com.axondragonscale.limitless.ui.editor.EditorVM
import com.axondragonscale.limitless.ui.home.HomeVM
import com.axondragonscale.limitless.ui.settings.SettingsVM
import com.axondragonscale.limitless.ui.stats.StatsVM
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 01/11/24
 */

val LimitlessModule = module {

    // Database
    singleOf(LimitlessDatabase::create)
    singleOf(LimitlessDatabase::habitDao)
    singleOf(LimitlessDatabase::recordDao)

    // Repository
    singleOf(::AppPrefsRepository)
    singleOf(::LimitlessRepository)

    // Dev Tools
    singleOf(::SampleDataSeeder)

    // ViewModel
    viewModelOf(::HomeVM)
    viewModelOf(::EditorVM)
    viewModelOf(::StatsVM)
    viewModelOf(::SettingsVM)

}
