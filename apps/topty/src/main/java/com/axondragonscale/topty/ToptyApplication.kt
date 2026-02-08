package com.axondragonscale.topty

import android.app.Application
import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.topty.di.ToptyModule
import com.axondragonscale.topty.di.WidgetModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */
class ToptyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@ToptyApplication)
            modules(
                CommonsModule,
                ToptyModule,
                WidgetModule,
            )
        }
    }
}
