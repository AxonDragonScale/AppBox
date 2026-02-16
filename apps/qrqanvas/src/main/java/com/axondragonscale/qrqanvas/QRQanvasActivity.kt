package com.axondragonscale.qrqanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.isDarkTheme
import com.axondragonscale.qrqanvas.repository.AppPrefsRepository
import com.axondragonscale.qrqanvas.ui.QRQanvasApp
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

/**
 * Created by Ronak Harkhani on 13/02/26
 */
class QRQanvasActivity : ComponentActivity() {

    private val appPrefsRepository: AppPrefsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialUiMode = runBlocking { appPrefsRepository.uiMode.get() }
        val initialDynamicTheme = runBlocking { appPrefsRepository.dynamicTheme.get() }

        setContent {
            val uiMode by appPrefsRepository.uiMode.flow.collectAsStateWithLifecycle(initialValue = initialUiMode)
            val dynamicTheme by appPrefsRepository.dynamicTheme.flow.collectAsStateWithLifecycle(initialValue = initialDynamicTheme)

            QRQanvasTheme(
                darkTheme = uiMode.isDarkTheme(),
                dynamicColor = dynamicTheme,
            ) {
                QRQanvasApp()
            }
        }
    }

}
