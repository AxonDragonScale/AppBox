package com.axondragonscale.topty.di

import com.axondragonscale.topty.auth.BiometricAvailabilityChecker
import com.axondragonscale.topty.backup.BackupExporter
import com.axondragonscale.topty.backup.BackupImporter
import com.axondragonscale.topty.core.TTS
import com.axondragonscale.topty.database.ToptyDatabase
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.security.PasswordManager
import com.axondragonscale.topty.ui.backuprestore.BackupRestoreVM
import com.axondragonscale.topty.ui.token.TokenVM
import com.axondragonscale.topty.ui.home.HomeVM
import com.axondragonscale.topty.ui.onboarding.OnboardingVM
import com.axondragonscale.topty.ui.qrscanner.QRScannerVM
import com.axondragonscale.topty.ui.settings.ChangePasswordVM
import com.axondragonscale.topty.ui.settings.SettingsVM
import com.axondragonscale.topty.ui.sharetoken.ShareTokenVM
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
    singleOf(::ToptyPrefsRepository)

    // Backup
    factoryOf(::BackupExporter)
    factoryOf(::BackupImporter)

    // Security
    singleOf(::PasswordManager)

    // Core
    singleOf(::TTS)

    // Auth
    singleOf(::BiometricAvailabilityChecker)

    // ViewModel
    viewModelOf(::HomeVM)
    viewModelOf(::OnboardingVM)
    viewModelOf(::QRScannerVM)
    viewModelOf(::SettingsVM)
    viewModelOf(::ChangePasswordVM)
    viewModelOf(::TokenVM)
    viewModelOf(::BackupRestoreVM)
    viewModelOf(::ShareTokenVM)

}
