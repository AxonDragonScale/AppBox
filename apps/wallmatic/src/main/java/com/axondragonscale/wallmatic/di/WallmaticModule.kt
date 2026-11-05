package com.axondragonscale.wallmatic.di

import com.axondragonscale.wallmatic.background.FallbackWorker
import com.axondragonscale.wallmatic.background.SyncWorker
import com.axondragonscale.wallmatic.background.TickScheduler
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.AlbumManager
import com.axondragonscale.wallmatic.core.FolderManager
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.core.WallpaperManager
import com.axondragonscale.wallmatic.core.WallpaperUpdater
import com.axondragonscale.wallmatic.database.WallmaticDatabase
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import com.axondragonscale.wallmatic.ui.album.AlbumVM
import com.axondragonscale.wallmatic.ui.albums.AlbumsVM
import com.axondragonscale.wallmatic.ui.folder.FolderVM
import com.axondragonscale.wallmatic.ui.home.HomeVM
import com.axondragonscale.wallmatic.ui.settings.SettingsVM
import com.axondragonscale.wallmatic.ui.wallpaper.WallpaperVM
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val WallmaticModule = module {
    // Database
    single { WallmaticDatabase.create(androidContext()) }
    single { get<WallmaticDatabase>().albumDao() }

    // Repositories & Managers
    singleOf(::AppPrefsRepository)
    singleOf(::WallmaticRepository)
    singleOf(::SyncManager)
    singleOf(::WallpaperManager)
    singleOf(::WallpaperUpdater)
    singleOf(::AlbumManager)
    singleOf(::FolderManager)

    singleOf(::TickScheduler)
    singleOf(::UpdateCoordinator)
    workerOf(::SyncWorker)
    workerOf(::FallbackWorker)

    // ViewModels
    viewModelOf(::SettingsVM)
    viewModelOf(::HomeVM)
    viewModelOf(::WallpaperVM)
    viewModelOf(::FolderVM)
    viewModelOf(::AlbumsVM)
    viewModelOf(::AlbumVM)
}
