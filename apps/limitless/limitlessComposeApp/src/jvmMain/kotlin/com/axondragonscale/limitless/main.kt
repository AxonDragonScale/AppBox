package com.axondragonscale.limitless

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.axondragonscale.commons.AppInfo
import com.axondragonscale.limitless.di.LimitlessJvmModule
import com.axondragonscale.limitless.di.initKoin
import com.axondragonscale.limitless.ui.LimitlessApp
import com.axondragonscale.limitless.ui.theme.LimitlessTheme

fun main() {
    AppInfo.init(
        versionName = "1.0.0",
        versionCode = 1,
        packageName = "com.axondragonscale.limitless",
    )

    initKoin(platformModule = LimitlessJvmModule)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Limitless",
        ) {
            LimitlessTheme {
                LimitlessApp()
            }
        }
    }
}
