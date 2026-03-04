package com.axondragonscale.topty

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.isDarkTheme
import com.axondragonscale.topty.auth.BiometricAuthManager
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.ui.BiometricLockScreen
import com.axondragonscale.topty.ui.Route
import com.axondragonscale.topty.ui.ToptyApp
import com.axondragonscale.topty.ui.theme.ToptyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class ToptyActivity : FragmentActivity() {

    private val prefsRepository: ToptyPrefsRepository by inject()
    private lateinit var biometricAuthManager: BiometricAuthManager
    private var isAuthenticated by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        biometricAuthManager = BiometricAuthManager(this)

        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        val initialUiMode = runBlocking { prefsRepository.uiMode.flow.first() }
        val initialDynamicTheme = runBlocking { prefsRepository.dynamicTheme.flow.first() }
        val initialUiFont = runBlocking { prefsRepository.uiFont.flow.first() }
        val hasCompletedOnboarding = runBlocking { prefsRepository.hasCompletedOnboarding.flow.first() }
        val biometricLockEnabled = runBlocking { prefsRepository.biometricLock.get() }
        val preventScreenshotsEnabled = runBlocking { prefsRepository.preventScreenshots.get() }

        // Apply screenshot prevention if enabled
        if (preventScreenshotsEnabled) window.setFlags(FLAG_SECURE, FLAG_SECURE)

        keepSplashScreen = false

        setContent {
            val uiMode by prefsRepository.uiMode.flow.collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by prefsRepository.dynamicTheme.flow.collectAsStateWithLifecycle(initialValue = initialDynamicTheme)
            val uiFont by prefsRepository.uiFont.flow.collectAsStateWithLifecycle(initialValue = initialUiFont)
            val preventScreenshots by prefsRepository.preventScreenshots.flow.collectAsStateWithLifecycle(initialValue = preventScreenshotsEnabled)

            LaunchedEffect(preventScreenshots) {
                if (preventScreenshots) window.setFlags(FLAG_SECURE, FLAG_SECURE)
                else window.clearFlags(FLAG_SECURE)
            }

            ToptyTheme(
                darkTheme = uiMode.isDarkTheme(),
                dynamicColor = dynamicTheme,
                uiFont = uiFont,
            ) {
                if (biometricLockEnabled && !isAuthenticated) {
                    BiometricLockScreen(
                        biometricAuthManager = biometricAuthManager,
                        prefsRepository = prefsRepository,
                        onAuthenticated = { isAuthenticated = true }
                    )
                } else {
                    ToptyApp(startDestination = if (hasCompletedOnboarding) Route.Home else Route.Onboarding)
                }
            }
        }
    }

}
