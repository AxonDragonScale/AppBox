package com.axondragonscale.devinator

import android.app.Application
import com.axondragonscale.commons.AppInfo
import com.axondragonscale.commons.di.CommonsModule
import com.axondragonscale.devinator.di.DevinatorModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/** Created by Ronak on 11/06/26 */
class DevinatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppInfo.init(this)
        startKoin {
            androidLogger()
            androidContext(this@DevinatorApplication)
            modules(CommonsModule, DevinatorModule)
        }
    }
}
