package com.axondragonscale.devinator.repository

import com.axondragonscale.commons.log
import com.axondragonscale.devinator.background.AutoOffScheduler
import com.axondragonscale.devinator.model.AdbEndpoint
import com.axondragonscale.devinator.model.AutoOffDuration
import com.axondragonscale.devinator.model.DevState
import com.axondragonscale.devinator.model.WriteResult
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Created by Ronak on 11/06/26 */

/** How often a write re-reads the key while SystemUI's trust dialog may be up. */
internal const val CONFIRM_POLL_ATTEMPTS = 10
internal const val CONFIRM_POLL_INTERVAL_MS = 250L

/**
 * What attempt [attempt] of the wireless-enable poll reports once the key was read as [confirmed].
 * Null means keep polling. The last attempt always answers, because a dialog the user declined
 * leaves the key at `0` forever.
 */
internal fun confirmPollOutcome(attempt: Int, confirmed: Boolean): WriteResult? = when {
    confirmed -> WriteResult.Ok
    attempt >= CONFIRM_POLL_ATTEMPTS - 1 -> WriteResult.AwaitingConsent
    else -> null
}

/**
 * The only writer of the three developer flags, and the single observable truth about them.
 *
 * Process-lifetime singleton: the observer has to outlive every screen, because Home, the setup gate
 * and the tiles must not disagree about what the settings database says. Only the last observed
 * value is kept — switch state is never persisted here.
 */
class DevSettingsRepository(
    private val settings: GlobalSettingsRepository,
    private val endpointsRepository: WirelessEndpointRepository,
    private val autoOff: AutoOffScheduler,
    private val prefs: AppPrefsRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(settings.readState())
    val state: StateFlow<DevState> = _state.asStateFlow()

    /** Candidate `adb connect` endpoints while wireless debugging is on; VPN first. */
    val endpoints: StateFlow<List<AdbEndpoint>> = endpointsRepository.endpoints

    /** Null when no auto-off timer is armed. */
    val autoOffDeadline: StateFlow<Instant?> = autoOff.deadline

    private val configuredDuration = MutableStateFlow(AppPrefsRepository.DEFAULT_AUTO_OFF_DURATION)

    init {
        scope.launch {
            // Hydrate before observing: the first sync arms the timer and reads this value.
            configuredDuration.value = prefs.autoOffDuration.get()
            autoOff.restoreDeadline()
            settings.observeState().collect(::sync)
        }
    }

    // region Writes

    suspend fun setDeveloperOptions(
        enabled: Boolean,
        alsoDisableDebugging: Boolean,
    ): WriteResult = guarded {
        settings.writeFlag(KEY_DEVELOPER_OPTIONS, enabled)
        if (!enabled && alsoDisableDebugging) {
            settings.writeFlag(KEY_USB_DEBUGGING, false)
            settings.writeFlag(KEY_WIRELESS_DEBUGGING, false)
        }
        WriteResult.Ok
    }

    suspend fun setUsbDebugging(enabled: Boolean): WriteResult = guarded {
        settings.writeFlag(KEY_USB_DEBUGGING, enabled)
        WriteResult.Ok
    }

    suspend fun setWirelessDebugging(enabled: Boolean): WriteResult = guarded {
        settings.writeFlag(KEY_WIRELESS_DEBUGGING, enabled)
        if (enabled) awaitConsent() else WriteResult.Ok
    }

    // endregion

    // region Auto-off timer

    /** [AutoOffDuration.OFF] removes the timer; the choice is persisted for the next session. */
    suspend fun setAutoOffDuration(duration: AutoOffDuration) {
        configuredDuration.value = duration
        prefs.autoOffDuration.set(duration)
        if (!state.value.wirelessDebugging) return
        val minutes = duration.minutes
        // A new duration restarts the countdown the user is looking at.
        if (minutes == null) autoOff.cancel() else autoOff.armAt(Clock.System.now() + minutes.minutes)
    }

    /** The configured auto-off choice. */
    fun autoOffDuration(): AutoOffDuration = configuredDuration.value

    // endregion

    private suspend fun sync(current: DevState) {
        _state.value = current
        try {
            if (current.wirelessDebugging) {
                endpointsRepository.start()
                ensureTimerArmed()
            } else {
                endpointsRepository.stop()
                // Unrelated keys change often; only touch the alarm when one is actually armed.
                if (autoOff.deadline.value != null) autoOff.cancel()
            }
        } catch (e: Exception) {
            log { e }
        }
    }

    /**
     * Keeps a timer armed while wireless debugging is on. Re-arming a deadline that is already
     * persisted also puts an alarm back behind it after a reboot, where the alarm was lost but the
     * preference survived.
     */
    private suspend fun ensureTimerArmed() {
        val minutes = configuredDuration.value.minutes
        val pending = autoOff.deadline.value?.takeIf { it > Clock.System.now() }
        when {
            minutes == null -> if (autoOff.deadline.value != null) autoOff.cancel()
            pending != null -> autoOff.armAt(pending)
            else -> autoOff.armAt(Clock.System.now() + minutes.minutes)
        }
    }

    /**
     * Enabling wireless debugging raises SystemUI's "Allow wireless debugging on this network?"
     * dialog, and the key keeps reading `0` until it is accepted, so a still-`0` key is a pending
     * consent rather than a failure.
     */
    private suspend fun awaitConsent(): WriteResult {
        var attempt = 0
        while (true) {
            val confirmed = settings.readFlag(KEY_WIRELESS_DEBUGGING)
            confirmPollOutcome(attempt, confirmed)?.let { return it }
            delay(CONFIRM_POLL_INTERVAL_MS)
            attempt++
        }
    }

    /** A failed write is a result, not an exception: the UI shows a banner and the switch snaps back. */
    private suspend fun guarded(block: suspend () -> WriteResult): WriteResult = try {
        block()
    } catch (e: Exception) {
        log { e }
        WriteResult.Failed("Could not change this setting")
    }
}

