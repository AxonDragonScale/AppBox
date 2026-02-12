package com.axondragonscale.wallmatic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.model.UIMode
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.ui.WallmaticApp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class WallmaticActivity : ComponentActivity() {

    private val appPrefsRepository: AppPrefsRepository by inject()
    private val syncManager: SyncManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        this.logD { "onCreate" }

        enableEdgeToEdge()

        val initialUiMode = runBlocking { appPrefsRepository.uiModeFlow.first() }
        val initialDynamicTheme = runBlocking { appPrefsRepository.dynamicThemeFlow.first() }

        // Sync albums in onStart creates a race condition when picking a folder or wallpaper.
        // Album sync and adding items to DB will happen at the same time when picker closes
        // and the app onStart is called.
        lifecycleScope.launch(Dispatchers.IO) {
            syncManager.syncAlbums()
        }

        setContent {
            val uiMode by appPrefsRepository.uiModeFlow
                .collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by appPrefsRepository.dynamicThemeFlow
                .collectAsStateWithLifecycle(initialValue = initialDynamicTheme)

            WallmaticTheme(
                darkTheme = isDarkTheme(uiMode),
                dynamicColor = dynamicTheme,
            ) {
                WallmaticApp()
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
