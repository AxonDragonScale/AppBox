package com.axondragonscale.jest

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.axondragonscale.commons.AppInfo
import com.axondragonscale.jest.ui.JestApp
import com.axondragonscale.jest.ui.theme.JestTheme

fun main() {
    AppInfo.init(
        versionName = "1.0.0",
        versionCode = 1,
        packageName = "com.axondragonscale.jest",
    )

    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Jest",
        ) {
            JestTheme {
                JestApp()
            }
        }
    }
}
