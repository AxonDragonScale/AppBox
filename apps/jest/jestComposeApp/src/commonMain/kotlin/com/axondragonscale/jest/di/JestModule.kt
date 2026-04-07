package com.axondragonscale.jest.di

import com.axondragonscale.commons.room.createDatabase
import com.axondragonscale.jest.database.JestDatabase
import com.axondragonscale.jest.database.JestDbClient
import com.axondragonscale.jest.network.JokeApiClient
import com.axondragonscale.jest.repository.JokeRepository
import com.axondragonscale.jest.repository.PrefsRepository
import com.axondragonscale.jest.ui.filter.PrefsVM
import com.axondragonscale.jest.ui.home.HomeVM
import com.axondragonscale.jest.ui.library.LibraryVM
import com.axondragonscale.jest.ui.settings.SettingsVM
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val JestModule = module {
    single { createDatabase<JestDatabase>(JestDatabase.DB_NAME) }
    single { get<JestDatabase>().getJokeDao() }

    single { JestDbClient(get()) }
    single { JokeApiClient(get()) }

    single { PrefsRepository() }
    single { JokeRepository(get(), get(), get()) }

    viewModel { HomeVM(get()) }
    viewModel { LibraryVM(get()) }
    viewModel { PrefsVM(get()) }
    viewModel { SettingsVM(get()) }
}
