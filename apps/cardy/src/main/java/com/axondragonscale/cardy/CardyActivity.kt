package com.axondragonscale.cardy

import android.content.Intent
import android.nfc.NfcAdapter
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
    private var isResumed = false
    private var isReadingPage = false
    private var isReaderModeEnabled = false
    private var readerModeReadingPage: Boolean? = null
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
                CardyApp(
                    onReadingPageChanged = {
                        isReadingPage = it
                        updateNfcReaderMode()
                    },
                )
            }
        }

        // Handle NFC intent that launched the activity
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        TAG.logD { "CardyActivity.onResume" }
        isResumed = true
        updateNfcReaderMode()
        homeVM.onEvent(HomeUiEvent.RefreshNfcStatus)
    }

    override fun onPause() {
        TAG.logD { "CardyActivity.onPause" }
        isResumed = false
        updateNfcReaderMode()
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

    private fun updateNfcReaderMode() {
        if (!isResumed) {
            if (!isReaderModeEnabled) return
            disableNfcReaderMode()
            isReaderModeEnabled = false
            readerModeReadingPage = null
            return
        }

        if (isReaderModeEnabled && readerModeReadingPage == isReadingPage) return
        enableNfcReaderMode()
        isReaderModeEnabled = true
        readerModeReadingPage = isReadingPage
    }

    private fun enableNfcReaderMode() {
        val adapter = nfcAdapter ?: run {
            TAG.logD { "CardyActivity.enableNfcReaderMode: no adapter, skipping" }
            return
        }
        adapter.enableReaderMode(
            this,
            { tag ->
                TAG.logD { "CardyActivity.readerCallback: tag techs=${tag.techList.joinToString()}" }
                if (!isReadingPage) {
                    TAG.logD { "CardyActivity.readerCallback: not on reading page, ignoring tag" }
                    return@enableReaderMode
                }
                handleNfcIntent(
                    Intent(NfcAdapter.ACTION_TECH_DISCOVERED)
                        .putExtra(NfcAdapter.EXTRA_TAG, tag),
                )
            },
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                if (isReadingPage) 0 else NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null,
        )
        TAG.logD { "CardyActivity.enableNfcReaderMode: reader mode enabled" }
    }

    private fun disableNfcReaderMode() {
        nfcAdapter?.disableReaderMode(this)
        TAG.logD { "CardyActivity.disableNfcReaderMode: reader mode disabled" }
    }
}
