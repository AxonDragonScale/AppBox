package com.axondragonscale.cardy

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.axondragonscale.cardy.nfc.NfcManager
import com.axondragonscale.cardy.ui.CardyApp
import com.axondragonscale.cardy.ui.home.HomeUiEvent
import com.axondragonscale.cardy.ui.home.HomeVM
import com.axondragonscale.cardy.ui.theme.CardyTheme
import com.axondragonscale.commons.logD
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "CardyActivity"

/** Created by Ronak on 05/30/26 */
class CardyActivity : FragmentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val nfcManager: NfcManager by inject()
    private val homeVM: HomeVM by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        TAG.logD { "CardyActivity.onCreate: homeVM instance=${homeVM.hashCode()}" }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        TAG.logD { "CardyActivity.onCreate: nfcAdapter=${nfcAdapter != null}" }

        setContent {
            CardyTheme {
                CardyApp()
            }
        }

        // Handle NFC intent that launched the activity
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        TAG.logD { "CardyActivity.onResume" }
        enableNfcForegroundDispatch()
        homeVM.onEvent(HomeUiEvent.RefreshNfcStatus)
    }

    override fun onPause() {
        TAG.logD { "CardyActivity.onPause" }
        disableNfcForegroundDispatch()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        TAG.logD { "CardyActivity.onNewIntent: action=${intent.action}, extras=${intent.extras?.keySet()}" }
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        TAG.logD { "CardyActivity.handleNfcIntent: action=${intent.action}" }
        if (nfcManager.isNfcIntent(intent)) {
            TAG.logD { "CardyActivity.handleNfcIntent: dispatching to homeVM (instance=${homeVM.hashCode()})" }
            homeVM.onEvent(HomeUiEvent.HandleNfcIntent(intent))
        } else {
            TAG.logD { "CardyActivity.handleNfcIntent: not an NFC intent, skipping" }
        }
    }

    private fun enableNfcForegroundDispatch() {
        val adapter = nfcAdapter ?: run {
            TAG.logD { "CardyActivity.enableNfcForegroundDispatch: no adapter, skipping" }
            return
        }
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        val techLists = arrayOf(
            arrayOf(IsoDep::class.java.name)
        )
        adapter.enableForegroundDispatch(this, pendingIntent, filters, techLists)
        TAG.logD { "CardyActivity.enableNfcForegroundDispatch: foreground dispatch enabled" }
    }

    private fun disableNfcForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
        TAG.logD { "CardyActivity.disableNfcForegroundDispatch: foreground dispatch disabled" }
    }
}
