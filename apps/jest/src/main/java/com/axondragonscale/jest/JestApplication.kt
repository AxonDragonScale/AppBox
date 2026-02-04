package com.axondragonscale.jest

import android.app.Application
import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.jest.di.JestModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */
class JestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@JestApplication)
            modules(
                CommonsModule,
                JestModule,
            )
        }
    }
}
