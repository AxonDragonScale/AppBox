package com.axondragonscale.limitless

import android.app.Application
import com.axondragonscale.commons.AppInfo
import com.axondragonscale.limitless.di.LimitlessAndroidModule
import com.axondragonscale.limitless.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Created by Ronak Harkhani on 30/10/24
 */
class LimitlessApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppInfo.init(this)
        initKoin(platformModule = LimitlessAndroidModule) {
            androidLogger()
            androidContext(this@LimitlessApplication)
        }
    }

}
