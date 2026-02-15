package com.axondragonscale.qrqanvas.di

import com.axondragonscale.qrqanvas.core.QRExporter
import com.axondragonscale.qrqanvas.database.QRQanvasDatabase
import com.axondragonscale.qrqanvas.repository.AppPrefsRepository
import com.axondragonscale.qrqanvas.repository.QRCodeRepository
import com.axondragonscale.qrqanvas.repository.ScanHistoryRepository
import com.axondragonscale.qrqanvas.repository.TemplateRepository
import com.axondragonscale.qrqanvas.ui.creator.CreatorVM
import com.axondragonscale.qrqanvas.ui.detail.DetailVM
import com.axondragonscale.qrqanvas.ui.history.HistoryVM
import com.axondragonscale.qrqanvas.ui.home.HomeVM
import com.axondragonscale.qrqanvas.ui.scanner.ScannerVM
import com.axondragonscale.qrqanvas.ui.settings.SettingsVM
import com.axondragonscale.qrqanvas.ui.templates.TemplatesVM
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 13/02/26
 */
val QRQanvasModule = module {
    // Database
    single { QRQanvasDatabase.create(androidContext()) }
    single { get<QRQanvasDatabase>().qrCodeDao() }
    single { get<QRQanvasDatabase>().scanHistoryDao() }
    single { get<QRQanvasDatabase>().templateDao() }

    // Core
    singleOf(::QRExporter)

    // Repositories
    singleOf(::AppPrefsRepository)
    singleOf(::QRCodeRepository)
    singleOf(::ScanHistoryRepository)
    singleOf(::TemplateRepository)

    // ViewModels
    viewModelOf(::CreatorVM)
    viewModelOf(::DetailVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::HomeVM)
    viewModelOf(::ScannerVM)
    viewModelOf(::SettingsVM)
    viewModelOf(::TemplatesVM)
}
