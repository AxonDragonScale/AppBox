package com.axondragonscale.cardy

import android.app.Application
import com.axondragonscale.cardy.di.CardyModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/** Created by Ronak on 05/30/26 */
class CardyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CardyApplication)
            modules(CardyModule)
        }
    }
}
