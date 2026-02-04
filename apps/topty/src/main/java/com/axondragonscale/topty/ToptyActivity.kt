package com.axondragonscale.topty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.topty.model.UIMode
import com.axondragonscale.topty.repository.AppPrefsRepository
import com.axondragonscale.topty.ui.ToptyApp
import com.axondragonscale.topty.ui.theme.ToptyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class ToptyActivity : ComponentActivity() {

    private val appPrefsRepository: AppPrefsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        val (initialUiMode, initialDynamicTheme, initialUiFont) = runBlocking {
            Triple(
                appPrefsRepository.uiMode.flow.first(),
                appPrefsRepository.dynamicTheme.flow.first(),
                appPrefsRepository.uiFont.flow.first(),
            )
        }
        keepSplashScreen = false

        setContent {
            val uiMode by appPrefsRepository.uiMode.flow
                .collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by appPrefsRepository.dynamicTheme.flow
                .collectAsStateWithLifecycle(initialValue = initialDynamicTheme)
            val uiFont by appPrefsRepository.uiFont.flow
                .collectAsStateWithLifecycle(initialValue = initialUiFont)

            ToptyTheme(
                darkTheme = isDarkTheme(uiMode),
                dynamicColor = dynamicTheme,
                uiFont = uiFont,
            ) {
                ToptyApp()
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
