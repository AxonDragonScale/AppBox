package com.axondragonscale.wallmatic.di

import androidx.work.WorkManager
import com.axondragonscale.wallmatic.background.FallbackWorker
import com.axondragonscale.wallmatic.background.SyncWorker
import com.axondragonscale.wallmatic.background.TickScheduler
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.AlbumEditor
import com.axondragonscale.wallmatic.core.AlbumSyncer
import com.axondragonscale.wallmatic.core.ConfigReconciler
import com.axondragonscale.wallmatic.core.WallpaperApplier
import com.axondragonscale.wallmatic.core.WallpaperCycler
import com.axondragonscale.wallmatic.database.WallmaticDatabase
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.PreferencesRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.ui.album.AlbumVM
import com.axondragonscale.wallmatic.ui.folder.FolderVM
import com.axondragonscale.wallmatic.ui.home.HomeVM
import com.axondragonscale.wallmatic.ui.library.LibraryVM
import com.axondragonscale.wallmatic.ui.settings.SettingsVM
import com.axondragonscale.wallmatic.ui.wallpaper.WallpaperVM
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val WallmaticModule = module {
    single { WallmaticDatabase.create(androidContext()) }
    single { get<WallmaticDatabase>().dao() }

    singleOf(::ConfigRepository)
    singleOf(::PreferencesRepository)
    singleOf(::WallmaticRepository)
    singleOf(::AlbumEditor)
    singleOf(::AlbumSyncer)
    singleOf(::WallpaperApplier)
    singleOf(::WallpaperCycler)
    singleOf(::ConfigReconciler)
    singleOf(::TickScheduler)
    singleOf(::UpdateCoordinator)
    single { WorkManager.getInstance(androidContext()) }

    workerOf(::SyncWorker)
    workerOf(::FallbackWorker)

    viewModelOf(::SettingsVM)
    viewModelOf(::HomeVM)
    viewModelOf(::WallpaperVM)
    viewModelOf(::FolderVM)
    viewModelOf(::LibraryVM)
    viewModelOf(::AlbumVM)
}
