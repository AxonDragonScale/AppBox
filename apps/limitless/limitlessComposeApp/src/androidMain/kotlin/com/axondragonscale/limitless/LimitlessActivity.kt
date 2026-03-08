package com.axondragonscale.limitless

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.isDarkTheme
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.ui.LimitlessApp
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import com.axondragonscale.limitless.ui.theme.SystemBarsEffect
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

        val initialUiMode = runBlocking { appPrefsRepository.uiMode.get() }
        val initialDynamicTheme = runBlocking { appPrefsRepository.dynamicTheme.get() }
        keepSplashScreen = false

        setContent {
            val uiMode by appPrefsRepository.uiMode.flow.collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by appPrefsRepository.dynamicTheme.flow.collectAsStateWithLifecycle(initialValue = initialDynamicTheme)

            val darkTheme = uiMode.isDarkTheme()

            val dynamicColorScheme = if (dynamicTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
            } else null

            SystemBarsEffect(darkTheme = darkTheme)

            LimitlessTheme(
                darkTheme = darkTheme,
                colorScheme = dynamicColorScheme,
            ) {
                LimitlessApp()
            }
        }
    }

}
