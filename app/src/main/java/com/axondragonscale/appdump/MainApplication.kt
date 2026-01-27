package com.axondragonscale.appdump

import android.app.Application
import com.axondragonscale.appdump.commons.logD
import com.axondragonscale.appdump.jest.di.JestModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by Ronak Harkhani on 26 January 2026.
 */
class MainApplication: Application() {

    companion object {
        private const val TAG = "MainApplication"
    }

    override fun onCreate() {
        super.onCreate()
        TAG.logD { "onCreate" }

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(JestModule)
        }

    }

}