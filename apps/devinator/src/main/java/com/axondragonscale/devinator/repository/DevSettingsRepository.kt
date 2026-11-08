package com.axondragonscale.devinator.repository

import com.axondragonscale.commons.log
import com.axondragonscale.devinator.background.DevAlarms
import com.axondragonscale.devinator.model.AdbEndpoint
import com.axondragonscale.devinator.model.DevState
import com.axondragonscale.devinator.model.DurationOption
import com.axondragonscale.devinator.model.WriteResult
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Created by Ronak on 11/06/26 */

/** How long a wireless-enable write waits for SystemUI's trust dialog before reporting it as pending. */
private val CONFIRM_TIMEOUT = 3.seconds

/**
 * The only writer of the three developer flags, and the single observable truth about them.
 *
 * Process-lifetime singleton: the observer has to outlive every screen, because Home, the setup gate
 * and the tiles must not disagree about what the settings database says. Only the last observed
 * value is kept — switch state is never persisted here. Turning developer options off writes a
 * restore snapshot to prefs; that is an intent, not cached state.
 */
class DevSettingsRepository(
    private val settings: GlobalSettingsRepository,
    private val endpointsRepository: WirelessEndpointRepository,
    private val alarms: DevAlarms,
    private val prefs: AppPrefsRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Serialises the developer-options cascade: snapshot, zero, restore. */
    private val developerMutex = Mutex()

    private val _state = MutableStateFlow(settings.readState())
    val state: StateFlow<DevState> = _state.asStateFlow()

    /** Candidate `adb connect` endpoints while wireless debugging is on; Wi-Fi first. */
    val endpoints: StateFlow<List<AdbEndpoint>> = endpointsRepository.endpoints

    private val _autoOffDuration = MutableStateFlow(AppPrefsRepository.DEFAULT_AUTO_OFF_DURATION)

    /** The configured auto-off choice. */
    val autoOffDuration: StateFlow<DurationOption> = _autoOffDuration.asStateFlow()

    private val _autoOffDeadline = MutableStateFlow<Instant?>(null)

    /** Null when no auto-off timer is armed. */
    val autoOffDeadline: StateFlow<Instant?> = _autoOffDeadline.asStateFlow()

    private val json = Json

    /** The last developer-options value seen, so an external off→on transition can replay the snapshot. */
    private var previousDeveloperOptions: Boolean? = null

    init {
        scope.launch {
            // Hydrate before observing: the first wireless emission arms the timer from these.
            _autoOffDuration.value = prefs.autoOffDuration.get()
            _autoOffDeadline.value = prefs.autoOffDeadline.get()?.let(Instant::fromEpochMilliseconds)
            launch {
                _state.map { it.wirelessDebugging }
                    .distinctUntilChanged()
                    .collect(::onWirelessChanged)
            }
            settings.observeState().collect(::sync)
        }
    }

    // region Writes

    /**
     * Turning developer options off remembers every restorable flag and then zeroes it; turning it
     * back on writes the remembered values. The screen asks nothing. The live key makes duplicate
     * taps idempotent, so a second off never overwrites the first snapshot.
     */
    suspend fun setDeveloperOptions(enabled: Boolean): WriteResult = guarded {
        developerMutex.withLock {
            if (settings.readFlag(KEY_DEVELOPER_OPTIONS) == enabled) return@withLock WriteResult.Ok
            if (enabled) {
                writeVerified(KEY_DEVELOPER_OPTIONS, true)
                restoreDeveloperFlagsLocked()
            } else {
                disableDeveloperOptionsLocked()
            }
            WriteResult.Ok
        }
    }

    private suspend fun disableDeveloperOptionsLocked() {
        val snapshot = RESTORABLE_FLAG_KEYS.associateWith { settings.readFlag(it) }
        prefs.developerFlagsSnapshot.set(json.encodeToString(snapshot))
        writeVerified(KEY_DEVELOPER_OPTIONS, false)
        RESTORABLE_FLAG_KEYS.forEach { writeVerified(it, false) }
    }

    /**
     * Consumes the snapshot. A stale snapshot describes an off-write the user has since moved past,
     * so it is dropped rather than applied when any restorable flag is already on.
     */
    private suspend fun restoreDeveloperFlags() = developerMutex.withLock {
        restoreDeveloperFlagsLocked()
    }

    private suspend fun restoreDeveloperFlagsLocked() {
        val raw = prefs.developerFlagsSnapshot.get() ?: return
        val snapshot = try {
            json.decodeFromString<Map<String, Boolean>>(raw)
        } catch (e: Exception) {
            log { e }
            prefs.developerFlagsSnapshot.set(null)
            return
        }

        if (RESTORABLE_FLAG_KEYS.any { settings.readFlag(it) }) {
            prefs.developerFlagsSnapshot.set(null)
            return
        }

        try {
            snapshot.forEach { (key, enabled) ->
                if (key in RESTORABLE_FLAG_KEYS) writeRestored(key, enabled)
            }
        } finally {
            prefs.developerFlagsSnapshot.set(null)
        }
    }

    /**
     * Wireless enable is the one write that legitimately stays `0` until the trust dialog is
     * accepted, so a restore cannot read it back. Disabling is silent and is verified like the rest.
     */
    private fun writeRestored(key: String, enabled: Boolean) {
        if (key == KEY_WIRELESS_DEBUGGING && enabled) {
            settings.writeFlag(key, true)
        } else {
            writeVerified(key, enabled)
        }
    }

    suspend fun setUsbDebugging(enabled: Boolean): WriteResult = guarded {
        if (enabled && !isDeveloperOptionsOn()) {
            WriteResult.Failed
        } else {
            writeVerified(KEY_USB_DEBUGGING, enabled)
            WriteResult.Ok
        }
    }

    suspend fun setWirelessDebugging(enabled: Boolean): WriteResult = guarded {
        if (enabled) {
            if (!isDeveloperOptionsOn()) return@guarded WriteResult.Failed
            val accepted = settings.writeFlag(KEY_WIRELESS_DEBUGGING, true)
            if (!accepted && !settings.readFlag(KEY_WIRELESS_DEBUGGING)) {
                WriteResult.Failed
            } else {
                awaitConsent()
            }
        } else {
            writeVerified(KEY_WIRELESS_DEBUGGING, false)
            WriteResult.Ok
        }
    }

    /**
     * A provider can accept the call and still ignore it (the OriginOS risk), so every ordinary write
     * is read back. A wireless enable is the exception: its key legitimately stays `0` until consent.
     */
    private fun writeVerified(key: String, enabled: Boolean) {
        settings.writeFlag(key, enabled)
        if (settings.readFlag(key) != enabled) error("Settings.Global did not apply $key")
    }

    // endregion

    // region Auto-off timer

    /** [DurationOption.OFF] removes the timer; the choice is persisted for the next session. */
    suspend fun setAutoOffDuration(duration: DurationOption) {
        _autoOffDuration.value = duration
        prefs.autoOffDuration.set(duration)
        if (!isWirelessDebuggingOn()) return
        val minutes = duration.minutes
        if (minutes == null) clearAutoOff() else armAutoOff(minutes)
    }

    /** The live key, for a user action that must not act on an observer snapshot. */
    fun isDeveloperOptionsOn(): Boolean = settings.readFlag(KEY_DEVELOPER_OPTIONS)

    fun isWirelessDebuggingOn(): Boolean = settings.readFlag(KEY_WIRELESS_DEBUGGING)

    /** The alarm fired: the deadline it belonged to is spent, so turn wireless debugging off. */
    suspend fun onAutoOffAlarm() {
        clearAutoOff()
        setWirelessDebugging(false)
    }

    /** A reboot drops the alarm but keeps the pref, so re-arm or act on the persisted deadline. */
    suspend fun reconcileAfterBoot() {
        _autoOffDuration.value = prefs.autoOffDuration.get()
        _autoOffDeadline.value = prefs.autoOffDeadline.get()?.let(Instant::fromEpochMilliseconds)
        if (!settings.readFlag(KEY_WIRELESS_DEBUGGING)) {
            clearAutoOff()
            return
        }
        reconcileAutoOff(allowNew = false)
    }

    /** The UI saw a deadline pass; the alarm may have been dropped, so act on it now. */
    suspend fun reconcileDeadline() {
        val deadline = _autoOffDeadline.value ?: return
        if (!isWirelessDebuggingOn()) {
            clearAutoOff()
            return
        }
        if (deadline <= Clock.System.now()) setWirelessDebugging(false)
    }

    // endregion

    private suspend fun onWirelessChanged(wireless: Boolean) {
        if (!wireless) {
            endpointsRepository.stop()
            clearAutoOff()
            return
        }

        endpointsRepository.start()
        reconcileAutoOff(allowNew = true)
    }

    private suspend fun reconcileAutoOff(allowNew: Boolean) {
        val minutes = _autoOffDuration.value.minutes
        if (minutes == null) {
            clearAutoOff()
            return
        }

        val deadline = _autoOffDeadline.value
        when {
            deadline == null -> if (allowNew) armAutoOff(minutes) else clearAutoOff()
            deadline <= Clock.System.now() -> setWirelessDebugging(false)
            else -> alarms.autoOff.armAt(deadline)
        }
    }

    private suspend fun armAutoOff(minutes: Int) {
        val deadline = Clock.System.now() + minutes.minutes
        prefs.autoOffDeadline.set(deadline.toEpochMilliseconds())
        _autoOffDeadline.value = deadline
        alarms.autoOff.armAt(deadline)
    }

    private suspend fun clearAutoOff() {
        alarms.autoOff.cancel()
        prefs.autoOffDeadline.set(null)
        _autoOffDeadline.value = null
    }

    private suspend fun sync(current: DevState) {
        val wasOn = previousDeveloperOptions
        previousDeveloperOptions = current.developerOptions
        _state.value = current
        try {
            // Enabling developer options in Settings has no write path of its own to restore from.
            if (current.developerOptions && wasOn != true) restoreDeveloperFlags()
        } catch (e: Exception) {
            log { e }
        }
    }

    /**
     * Enabling wireless debugging raises SystemUI's "Allow wireless debugging on this network?"
     * dialog, and the key keeps reading `0` until it is accepted, so a still-`0` key is a pending
     * consent rather than a failure.
     */
    private suspend fun awaitConsent(): WriteResult {
        val confirmed = withTimeoutOrNull(CONFIRM_TIMEOUT) {
            state.first { it.wirelessDebugging }
        }
        return if (confirmed != null) WriteResult.Ok else WriteResult.AwaitingConsent
    }

    /** A failed write is a result, not an exception: the UI shows a snackbar and the switch snaps back. */
    private suspend fun guarded(block: suspend () -> WriteResult): WriteResult = try {
        block()
    } catch (e: Exception) {
        log { e }
        WriteResult.Failed
    }
}
