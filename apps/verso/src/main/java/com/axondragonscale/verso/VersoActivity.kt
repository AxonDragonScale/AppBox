package com.axondragonscale.verso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.axondragonscale.verso.ui.VersoApp
import com.axondragonscale.verso.ui.theme.VersoTheme

class VersoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VersoTheme {
                VersoApp()
            }
        }
    }
}
