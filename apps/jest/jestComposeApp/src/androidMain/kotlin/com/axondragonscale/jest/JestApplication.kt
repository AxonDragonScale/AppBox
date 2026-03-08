package com.axondragonscale.jest

import android.app.Application
import com.axondragonscale.commons.AppInfo
import com.axondragonscale.jest.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Created by Ronak Harkhani on 27 January 2026.
 */
class JestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppInfo.init(this)
        initKoin {
            androidLogger()
            androidContext(this@JestApplication)
        }
    }
}
