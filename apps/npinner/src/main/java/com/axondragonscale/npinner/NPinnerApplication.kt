package com.axondragonscale.npinner

import android.app.Application
import com.axondragonscale.npinner.di.NPinnerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

/**
 * Created by Ronak Harkhani on 22/04/23
 */
class NPinnerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@NPinnerApplication)
            workManagerFactory()
            modules(
                NPinnerModule,
            )
        }
    }
}
