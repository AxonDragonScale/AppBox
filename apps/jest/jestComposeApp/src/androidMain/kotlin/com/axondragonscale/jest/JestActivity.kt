package com.axondragonscale.jest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.isDarkTheme
import com.axondragonscale.jest.repository.PrefsRepository
import com.axondragonscale.jest.ui.theme.JestTheme
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class JestActivity : ComponentActivity() {

    private val prefsRepository: PrefsRepository by inject()
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val (initialUiMode, initialDynamicTheme) = runBlocking {
            prefsRepository.uiMode.get() to prefsRepository.dynamicTheme.get()
        }

        setContent {
            val uiMode by prefsRepository.uiMode.flow
                .collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by prefsRepository.dynamicTheme.flow
                .collectAsStateWithLifecycle(initialValue = initialDynamicTheme)

            val darkTheme = uiMode.isDarkTheme()

            val dynamicColorScheme = if (dynamicTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(this)
                else dynamicLightColorScheme(this)
            } else null

            val view = LocalView.current
            if (!view.isInEditMode) {
                // Resolve the actual color scheme for system bar tinting
                val resolvedScheme = dynamicColorScheme
                    ?: if (darkTheme) {
                        androidx.compose.material3.darkColorScheme(
                            primary = com.axondragonscale.jest.ui.theme.Purple80,
                            secondary = com.axondragonscale.jest.ui.theme.PurpleGrey80,
                            tertiary = com.axondragonscale.jest.ui.theme.Pink80,
                        )
                    } else {
                        androidx.compose.material3.lightColorScheme(
                            primary = com.axondragonscale.jest.ui.theme.Purple40,
                            secondary = com.axondragonscale.jest.ui.theme.PurpleGrey40,
                            tertiary = com.axondragonscale.jest.ui.theme.Pink40,
                        )
                    }

                SideEffect {
                    val window = this@JestActivity.window
                    window.statusBarColor = resolvedScheme.primary.toArgb()
                    window.navigationBarColor = resolvedScheme.background.toArgb()
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
            }

            JestTheme(
                darkTheme = darkTheme,
                colorScheme = dynamicColorScheme,
            ) {
                JestApp()
            }
        }

        setupNotificationPermission()
    }

    private fun setupNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            if (status != PackageManager.PERMISSION_GRANTED)
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
