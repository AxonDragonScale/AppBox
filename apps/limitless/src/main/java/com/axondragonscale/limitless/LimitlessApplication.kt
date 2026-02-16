package com.axondragonscale.limitless

import android.app.Application
import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.limitless.di.LimitlessModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by Ronak Harkhani on 30/10/24
 */
class LimitlessApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LimitlessApplication)
            androidLogger()
            modules(
                CommonsModule,
                LimitlessModule,
            )
        }
    }

}
