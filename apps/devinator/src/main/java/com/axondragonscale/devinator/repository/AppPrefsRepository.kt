package com.axondragonscale.devinator.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.UIMode
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.nullablePref
import com.axondragonscale.commons.pref
import com.axondragonscale.devinator.model.DurationOption

/** Created by Ronak on 11/06/26 */

private const val APP_PREFS_STORE = "devinator_prefs"

private val Context.appPrefs by preferencesDataStore(APP_PREFS_STORE)

/**
 * Everything Devinator remembers across launches, and the only place that needs a `Context` for
 * preferences. The wrappers in `:core:commons` already absorb store failures, so these reads answer
 * with the stored value, the default, or null — never an exception.
 */
class AppPrefsRepository(
    context: Context,
) {

    companion object {
        /** Auto-off duration used until the user picks another one. */
        val DEFAULT_AUTO_OFF_DURATION = DurationOption.OFF

        /** Timed-toggle duration used until the user picks another one. */
        val DEFAULT_TIMED_TOGGLE_DURATION = DurationOption.MIN_5
    }

    private val dataStore = context.appPrefs

    /** System by default: the app has always followed the device's dark mode. */
    val uiMode = dataStore.enumPref(stringPreferencesKey("ui_mode"), UIMode.DEFAULT)

    /** Material You over the app's own palette; off, because only the fallback palette is reviewed. */
    val dynamicColor = dataStore.pref(booleanPreferencesKey("dynamic_color"), false)

    val autoOffDuration = dataStore.enumPref(stringPreferencesKey("auto_off_duration"), DEFAULT_AUTO_OFF_DURATION)

    val timedToggleDuration =
        dataStore.enumPref(stringPreferencesKey("timed_toggle_duration"), DEFAULT_TIMED_TOGGLE_DURATION)

    /** Stored as epoch milliseconds; null when no timed-toggle session is armed. */
    val timedToggleDeadline = dataStore.nullablePref(longPreferencesKey("timed_toggle_deadline"))

    /** The Developer options state captured when the timed session started. */
    val timedToggleRestoreTo = dataStore.nullablePref(booleanPreferencesKey("timed_toggle_restore_to"))

    /** Stored as epoch milliseconds; null when no auto-off deadline is armed. */
    val autoOffDeadline = dataStore.nullablePref(longPreferencesKey("auto_off_deadline"))

    /**
     * The developer flags captured when developer options was turned off, as JSON. Null when there
     * is nothing to restore.
     */
    val developerFlagsSnapshot =
        dataStore.nullablePref(stringPreferencesKey("developer_flags_snapshot"))
}
