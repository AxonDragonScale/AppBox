/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.di

import com.axondragonscale.cardy.database.CardyDatabase
import com.axondragonscale.cardy.nfc.CardReader
import com.axondragonscale.cardy.nfc.NfcManager
import com.axondragonscale.cardy.repository.CardRepository
import com.axondragonscale.cardy.security.BiometricAuthManager
import com.axondragonscale.cardy.ui.detail.DetailVM
import com.axondragonscale.cardy.ui.home.HomeVM
import com.axondragonscale.cardy.ui.vault.VaultVM
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val CardyModule = module {
    single { CardyDatabase.create(androidContext()) }
    single { get<CardyDatabase>().cardDao() }
    single { NfcManager(androidContext()) }
    single { CardReader() }
    single { BiometricAuthManager() }
    single { CardRepository(get()) }
    viewModelOf(::HomeVM)
    viewModelOf(::VaultVM)
    viewModelOf(::DetailVM)
}
