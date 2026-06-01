package com.axondragonscale.cardy.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "NfcManager"

/** Created by Ronak on 05/30/26 */
class NfcManager(private val context: Context) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    val isNfcSupported: Boolean get() = nfcAdapter != null
    val isNfcEnabled: Boolean get() = nfcAdapter?.isEnabled == true

    // Reactive NFC state via BroadcastReceiver
    private val _nfcEnabledState = MutableStateFlow(isNfcEnabled)
    val nfcEnabledState: StateFlow<Boolean> = _nfcEnabledState.asStateFlow()

    private val nfcStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) {
                val state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)
                TAG.logD { "NfcManager: adapter state changed -> $state (OFF=1, TURNING_ON=2, ON=3, TURNING_OFF=4)" }
                when (state) {
                    NfcAdapter.STATE_ON -> _nfcEnabledState.value = true
                    NfcAdapter.STATE_OFF -> _nfcEnabledState.value = false
                    NfcAdapter.STATE_TURNING_ON -> TAG.logD { "NfcManager: NFC turning ON..." }
                    NfcAdapter.STATE_TURNING_OFF -> TAG.logD { "NfcManager: NFC turning OFF..." }
                }
            }
        }
    }

    init {
        TAG.logD { "NfcManager: init, adapter=${nfcAdapter != null}, enabled=$isNfcEnabled" }
        // Register receiver for NFC state changes
        val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        context.registerReceiver(nfcStateReceiver, filter)
    }

    /**
     * Extract IsoDep from an NFC intent.
     * Returns null if the tag doesn't support IsoDep.
     */
    fun getIsoDep(intent: Intent): IsoDep? {
        TAG.logD { "NfcManager.getIsoDep: intent action=${intent.action}" }

        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        if (tag == null) {
            TAG.logE { "NfcManager.getIsoDep: EXTRA_TAG is null" }
            return null
        }

        TAG.logD { "NfcManager.getIsoDep: tag techs=${tag.techList.joinToString()}" }
        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            TAG.logE { "NfcManager.getIsoDep: IsoDep.get(tag) returned null — tag doesn't support IsoDep" }
        } else {
            TAG.logD { "NfcManager.getIsoDep: IsoDep obtained successfully" }
        }
        return isoDep
    }

    /**
     * Check if an intent is an NFC discovery intent.
     */
    fun isNfcIntent(intent: Intent): Boolean {
        val result = intent.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED
        TAG.logD { "NfcManager.isNfcIntent: action=${intent.action}, isNfc=$result" }
        return result
    }
}
