package com.axondragonscale.jest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.isDarkTheme
import com.axondragonscale.jest.repository.PrefsRepository
import com.axondragonscale.jest.ui.JestApp
import com.axondragonscale.jest.ui.theme.JestTheme
import kotlinx.coroutines.flow.first
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

            JestTheme(
                darkTheme = uiMode.isDarkTheme(),
                dynamicColor = dynamicTheme,
            ) {
                JestApp()
            }
        }

        setupChucker()
    }

    private fun setupChucker() {
        if (/*BuildConfig.DEBUG && */Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            if (status != PackageManager.PERMISSION_GRANTED)
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

}
