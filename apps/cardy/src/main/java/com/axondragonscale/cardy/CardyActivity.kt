package com.axondragonscale.cardy

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.axondragonscale.cardy.nfc.NfcManager
import com.axondragonscale.cardy.ui.CardyApp
import com.axondragonscale.cardy.ui.theme.CardyTheme
import org.koin.android.ext.android.inject

/** Created by Ronak on 05/30/26 */
class CardyActivity : FragmentActivity() {

    private val nfcManager: NfcManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CardyTheme {
                CardyApp()
            }
        }
        nfcManager.submitIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcManager.submitIntent(intent)
    }
}
