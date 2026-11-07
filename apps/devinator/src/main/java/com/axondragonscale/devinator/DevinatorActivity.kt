package com.axondragonscale.devinator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.isDarkTheme
import com.axondragonscale.devinator.repository.AppPrefsRepository
import com.axondragonscale.devinator.ui.DevinatorApp
import com.axondragonscale.devinator.ui.theme.DevinatorTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Created by Ronak on 11/06/26 */
class DevinatorActivity : ComponentActivity(), KoinComponent {

    private val prefs: AppPrefsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Read here rather than in the theme so a Settings change repaints the whole app.
            val uiMode by prefs.uiMode.flow.collectAsState(initial = UIMode.DEFAULT)
            val dynamicColor by prefs.dynamicColor.flow.collectAsState(initial = false)

            DevinatorTheme(
                darkTheme = uiMode.isDarkTheme(),
                dynamicColor = dynamicColor,
            ) {
                DevinatorApp()
            }
        }
    }
}
