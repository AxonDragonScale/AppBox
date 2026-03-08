package com.axondragonscale.limitless

import androidx.compose.ui.window.ComposeUIViewController
import com.axondragonscale.limitless.di.LimitlessIosModule
import com.axondragonscale.limitless.di.initKoin
import com.axondragonscale.limitless.ui.LimitlessApp
import com.axondragonscale.limitless.ui.theme.LimitlessTheme

fun MainViewController() = ComposeUIViewController {
    initKoin(platformModule = LimitlessIosModule)
    LimitlessTheme {
        LimitlessApp()
    }
}
