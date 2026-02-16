package com.axondragonscale.limitless

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.limitless.model.UIMode
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.ui.LimitlessApp
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class LimitlessActivity : ComponentActivity() {

    private val appPrefsRepository: AppPrefsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        val (initialUiMode, initialDynamicTheme) = runBlocking {
            Pair(
                appPrefsRepository.uiModeFlow.first(),
                appPrefsRepository.dynamicThemeFlow.first(),
            )
        }
        keepSplashScreen = false

        setContent {
            val uiMode by appPrefsRepository.uiModeFlow
                .collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by appPrefsRepository.dynamicThemeFlow
                .collectAsStateWithLifecycle(initialValue = initialDynamicTheme)

            LimitlessTheme(
                darkTheme = isDarkTheme(uiMode),
                dynamicColor = dynamicTheme,
            ) {
                LimitlessApp()
            }
        }
    }

    @Composable
    private fun isDarkTheme(uiMode: UIMode) = when (uiMode) {
        UIMode.LIGHT -> false
        UIMode.DARK -> true
        UIMode.AUTO -> isSystemInDarkTheme()
    }
}

