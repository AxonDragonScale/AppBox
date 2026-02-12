package com.axondragonscale.qrqanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.axondragonscale.qrqanvas.ui.QRQanvasApp
import com.axondragonscale.qrqanvas.ui.theme.QRQanvasTheme

/**
 * Created by Ronak Harkhani on 13/02/26
 */
class QRQanvasActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRQanvasTheme {
                QRQanvasApp()
            }
        }
    }
}
