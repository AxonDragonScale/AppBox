package com.axondragonscale.verso

import android.app.Application
import com.axondragonscale.verso.di.VersoModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class VersoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@VersoApplication)
            modules(VersoModule)
        }
    }
}
