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
import com.axondragonscale.wallmatic.repository.PreferencesRepository
import com.axondragonscale.wallmatic.ui.WallmaticApp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class WallmaticActivity : ComponentActivity() {

    private val preferencesRepository: PreferencesRepository by inject()
    private val updateCoordinator: UpdateCoordinator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        this.logD { "onCreate" }

        enableEdgeToEdge()

        val initialUiMode = runBlocking { preferencesRepository.uiMode.get() }
        val initialDynamicTheme = runBlocking { preferencesRepository.dynamicTheme.get() }

        // Syncing in onStart races with the picker closing and writing the same albums.
        lifecycleScope.launch(Dispatchers.IO) { updateCoordinator.refresh() }

        setContent {
            val uiMode by preferencesRepository.uiMode.flow
                .collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by preferencesRepository.dynamicTheme.flow
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
        lifecycleScope.launch(Dispatchers.IO) { updateCoordinator.applyIfDue() }
    }

}
