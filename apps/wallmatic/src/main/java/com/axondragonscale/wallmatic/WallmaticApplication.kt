package com.axondragonscale.wallmatic

import android.app.Application
import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.wallmatic.di.WallmaticModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */
class WallmaticApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@WallmaticApplication)
            workManagerFactory()
            modules(CommonsModule, WallmaticModule,)
        }
    }
}
