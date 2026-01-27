package com.axondragonscale.jest.di

import com.axondragonscale.jest.database.JestDatabase
import com.axondragonscale.jest.database.JestDatabaseClient
import com.axondragonscale.jest.network.JokeApiClient
import com.axondragonscale.jest.repository.JokeRepository
import com.axondragonscale.jest.repository.PrefsRepository
import com.axondragonscale.jest.ui.home.HomeVM
import com.axondragonscale.jest.ui.list.FavoritesVM
import com.axondragonscale.jest.ui.list.HistoryVM
import com.axondragonscale.jest.ui.tune.PrefsVM
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val JestModule = module {
    single { Json { ignoreUnknownKeys = true } }

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
