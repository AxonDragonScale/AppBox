package com.axondragonscale.appdump.jest.di

import com.axondragonscale.appdump.jest.database.JestDatabase
import com.axondragonscale.appdump.jest.database.JestDatabaseClient
import com.axondragonscale.appdump.jest.network.JokeApiClient
import com.axondragonscale.appdump.jest.repository.JokeRepository
import com.axondragonscale.appdump.jest.repository.PrefsRepository
import com.axondragonscale.appdump.jest.ui.home.HomeVM
import com.axondragonscale.appdump.jest.ui.list.FavoritesVM
import com.axondragonscale.appdump.jest.ui.list.HistoryVM
import com.axondragonscale.appdump.jest.ui.tune.PrefsVM
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val JestModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single { JestDatabase.create(get()) }
    single { get<JestDatabase>().getJokeDao() }

    single { JestDatabaseClient(get()) }
    single { JokeApiClient(get(), get()) }

    single { PrefsRepository(get()) }
    single { JokeRepository(get(), get(), get()) }

    viewModel { HomeVM(get()) }
    viewModel { FavoritesVM(get()) }
    viewModel { HistoryVM(get()) }
    viewModel { PrefsVM(get()) }
}
