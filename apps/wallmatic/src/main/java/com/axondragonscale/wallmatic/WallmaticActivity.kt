package com.axondragonscale.wallmatic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.axondragonscale.commons.isDarkTheme
import com.axondragonscale.commons.logD
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.SyncManager
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
    private val updateCoordinator: UpdateCoordinator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        this.logD { "onCreate" }

        enableEdgeToEdge()

        val initialUiMode = runBlocking { appPrefsRepository.uiModeFlow.first() }
        val initialDynamicTheme = runBlocking { appPrefsRepository.dynamicThemeFlow.first() }

        // Syncing in onStart races with the picker closing and writing the same albums.
        lifecycleScope.launch(Dispatchers.IO) {
            syncManager.syncAlbums()
        }

        setContent {
            val uiMode by appPrefsRepository.uiModeFlow
                .collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by appPrefsRepository.dynamicThemeFlow
                .collectAsStateWithLifecycle(initialValue = initialDynamicTheme)

            WallmaticTheme(
                darkTheme = uiMode.isDarkTheme(),
                dynamicColor = dynamicTheme,
            ) {
                WallmaticApp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch(Dispatchers.IO) { updateCoordinator.reconcile() }
    }

}
