package com.axondragonscale.cardy.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "NfcManager"

sealed interface NfcReaderMode {
    data class Read(val onTag: (Tag) -> Unit) : NfcReaderMode
    data object Suppress : NfcReaderMode
}

/** Created by Ronak on 05/30/26 */
class NfcManager(private val context: Context) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    @Volatile
    private var readerMode: NfcReaderMode? = null

    @Volatile
    private var pendingTag: Tag? = null

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

    fun submitIntent(intent: Intent) {
        if (!isNfcIntent(intent)) return
        val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        if (tag == null) {
            TAG.logE { "NfcManager.submitIntent: EXTRA_TAG is null" }
            return
        }
        dispatch(tag)
    }

    fun enableReaderMode(activity: FragmentActivity, mode: NfcReaderMode) {
        readerMode = mode
        val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
            if (mode == NfcReaderMode.Suppress) NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS else 0
        nfcAdapter?.enableReaderMode(activity, ::dispatch, flags, null)
        TAG.logD { "NfcManager.enableReaderMode: mode=${mode::class.simpleName}" }

        if (mode is NfcReaderMode.Read) {
            pendingTag?.let {
                pendingTag = null
                dispatch(it)
            }
        } else {
            pendingTag = null
        }
    }

    fun disableReaderMode(activity: FragmentActivity) {
        readerMode = null
        nfcAdapter?.disableReaderMode(activity)
        TAG.logD { "NfcManager.disableReaderMode" }
    }

    fun getIsoDep(tag: Tag): IsoDep? {
        TAG.logD { "NfcManager.getIsoDep: tag techs=${tag.techList.joinToString()}" }
        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            TAG.logE { "NfcManager.getIsoDep: IsoDep.get(tag) returned null — tag doesn't support IsoDep" }
        } else {
            TAG.logD { "NfcManager.getIsoDep: IsoDep obtained successfully" }
        }
        return isoDep
    }

    private fun dispatch(tag: Tag) {
        when (val mode = readerMode) {
            is NfcReaderMode.Read -> mode.onTag(tag)
            NfcReaderMode.Suppress -> TAG.logD { "NfcManager.dispatch: suppressing tag" }
            null -> pendingTag = tag
        }
    }

    private fun isNfcIntent(intent: Intent): Boolean {
        val result = intent.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED
        TAG.logD { "NfcManager.isNfcIntent: action=${intent.action}, isNfc=$result" }
        return result
    }
}
