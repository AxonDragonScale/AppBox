package com.axondragonscale.devinator.repository

import com.axondragonscale.devinator.background.DevAlarms
import com.axondragonscale.devinator.model.DevState
import com.axondragonscale.devinator.model.DurationOption
import com.axondragonscale.devinator.model.TimedToggleSession
import com.axondragonscale.devinator.model.WriteResult
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Created by Ronak on 08/11/26 */

/**
 * The policy engine for timed Developer options changes.
 *
 * One session exists at a time. A session stores the state that was active before it started and
 * restores that state at its deadline; the writes go through [DevSettingsRepository], so USB and
 * wireless debugging follow the existing snapshot and restore path. A manual Developer options
 * change cancels the session — the timer never fights the user.
 */
class TimedToggleRepository(
    private val settings: DevSettingsRepository,
    private val prefs: AppPrefsRepository,
    private val alarms: DevAlarms,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    private val _session = MutableStateFlow<TimedToggleSession?>(null)
    val session: StateFlow<TimedToggleSession?> = _session.asStateFlow()

    private val _duration = MutableStateFlow(AppPrefsRepository.DEFAULT_TIMED_TOGGLE_DURATION)
    val duration: StateFlow<DurationOption> = _duration.asStateFlow()

    /**
     * Set while our own write is travelling to `Settings.Global`; the observer ignores a
     * mismatched value until it sees the target, so the session is not cancelled by its own write.
     */
    private var pendingTarget: Boolean? = null

    init {
        scope.launch {
            _duration.value = prefs.timedToggleDuration.get()
            reconcile()
            settings.state.collect(::onDeveloperOptionsChanged)
        }
    }

    // region Actions

    /**
     * Applies [target] for the configured duration. A second tap on the same target ends the
     * session early; the opposite target switches direction and starts a fresh timer.
     *
     * Null means "already in that state, nothing to do".
     */
    suspend fun toggle(target: Boolean): WriteResult? = mutex.withLock {
        val minutes = _duration.value.minutes ?: return@withLock null
        val current = settings.isDeveloperOptionsOn()
        val active = _session.value

        if (active != null) {
            if (current == target) return@withLock endSessionLocked()
            // Switching direction: leave the old session armed until the new write succeeds, so a
            // failed write leaves the user in the state they were already in.
        } else if (current == target) {
            return@withLock null
        }

        val restoreTo = current
        pendingTarget = target
        val result = settings.setDeveloperOptions(target)
        if (result == WriteResult.Failed) {
            pendingTarget = null
            return@withLock result
        }

        val deadline = Clock.System.now() + minutes.minutes
        prefs.timedToggleRestoreTo.set(restoreTo)
        prefs.timedToggleDeadline.set(deadline.toEpochMilliseconds())
        alarms.timedToggle.armAt(deadline)
        _session.value = TimedToggleSession(target, restoreTo, deadline)
        result
    }

    /** Ends the active session early and restores the state from before it started. */
    suspend fun endSession(): WriteResult? = mutex.withLock { endSessionLocked() }

    /** Persists the duration and restarts the countdown when a session is live. */
    suspend fun setDuration(duration: DurationOption) {
        mutex.withLock {
            val minutes = duration.minutes ?: return@withLock
            _duration.value = duration
            prefs.timedToggleDuration.set(duration)
            val session = _session.value ?: return@withLock
            val deadline = Clock.System.now() + minutes.minutes
            prefs.timedToggleDeadline.set(deadline.toEpochMilliseconds())
            alarms.timedToggle.armAt(deadline)
            _session.value = session.copy(deadline = deadline)
        }
    }

    // endregion

    // region Recovery

    /**
     * Called at process start: an expired session is restored immediately, a live one gets its
     * alarm back (a force-stop drops alarms while leaving the pref behind).
     */
    private suspend fun reconcile() = mutex.withLock {
        val restoreTo = prefs.timedToggleRestoreTo.get()
        val deadline = prefs.timedToggleDeadline.get()?.let(Instant::fromEpochMilliseconds)

        if (restoreTo == null || deadline == null) {
            if (restoreTo != null || deadline != null) clearSessionLocked()
            return@withLock
        }

        val session = TimedToggleSession(
            target = !restoreTo,
            restoreTo = restoreTo,
            deadline = deadline,
        )
        _session.value = session
        if (deadline <= Clock.System.now()) {
            endSessionLocked()
        } else {
            alarms.timedToggle.armAt(deadline)
        }
    }

    /** A reboot ends the session; put the pre-session state back and clear the timer. */
    suspend fun restoreAfterReboot() {
        mutex.withLock {
            val restoreTo = prefs.timedToggleRestoreTo.get()
            clearSessionLocked()
            if (restoreTo != null) settings.setDeveloperOptions(restoreTo)
        }
    }

    /** The UI saw a deadline pass; the alarm may have been dropped, so act on it now. */
    suspend fun reconcileDeadline() = mutex.withLock {
        val session = _session.value ?: return@withLock
        if (session.deadline <= Clock.System.now()) endSessionLocked()
    }

    // endregion

    private suspend fun endSessionLocked(): WriteResult? {
        // Read the prefs as well as the in-memory session: the alarm receiver can be the first
        // thing to run in a fresh process, before reconcile() has hydrated the session flow.
        val restoreTo = prefs.timedToggleRestoreTo.get()
            ?: _session.value?.restoreTo
            ?: return null
        clearSessionLocked()
        return settings.setDeveloperOptions(restoreTo)
    }

    private suspend fun clearSessionLocked() {
        alarms.timedToggle.cancel()
        prefs.timedToggleRestoreTo.set(null)
        prefs.timedToggleDeadline.set(null)
        _session.value = null
        pendingTarget = null
    }

    private suspend fun onDeveloperOptionsChanged(state: DevState) = mutex.withLock {
        val session = _session.value ?: return@withLock
        val pending = pendingTarget
        if (pending != null) {
            if (state.developerOptions == pending) {
                pendingTarget = null
                return@withLock
            }
            // A different value overtook the write: the user changed it, so the session must not survive.
            pendingTarget = null
        }
        if (state.developerOptions != session.target) {
            clearSessionLocked()
        }
    }
}
