package com.axondragonscale.devinator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.axondragonscale.devinator.ui.DevinatorApp
import com.axondragonscale.devinator.ui.theme.DevinatorTheme

/** Created by Ronak on 11/06/26 */
class DevinatorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DevinatorTheme {
                DevinatorApp()
            }
        }
    }
}
