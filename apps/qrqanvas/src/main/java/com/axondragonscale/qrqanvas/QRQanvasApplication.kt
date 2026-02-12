package com.axondragonscale.qrqanvas

import android.app.Application
import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.qrqanvas.di.QRQanvasModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by Ronak Harkhani on 09/07/25
 */
class QRQanvasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@QRQanvasApplication)
            modules(
                CommonsModule,
                QRQanvasModule,
            )
        }
    }
}
