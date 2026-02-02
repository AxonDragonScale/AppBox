package com.axondragonscale.appbox

import android.app.Application
import com.axondragonscale.appbox.di.AppModule
import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.commons.logD
import com.axondragonscale.jest.di.JestModule
import com.axondragonscale.wallmatic.di.WallmaticModule
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
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                AppModule,
                CommonsModule,
                JestModule,
                WallmaticModule,
            )
        }
    }

}