package com.axondragonscale.devinator.repository

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.axondragonscale.commons.enumPref
import com.axondragonscale.commons.nullablePref
import com.axondragonscale.devinator.model.AutoOffDuration
import kotlin.time.Instant

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
        val DEFAULT_AUTO_OFF_DURATION = AutoOffDuration.MIN_30
    }

    private val dataStore = context.appPrefs

    private val autoOffDurationPref = dataStore.enumPref(
        stringPreferencesKey("auto_off_duration"),
        DEFAULT_AUTO_OFF_DURATION,
    )
    private val autoOffDeadlinePref = dataStore.nullablePref(longPreferencesKey("auto_off_deadline"))

    suspend fun autoOffDuration(): AutoOffDuration = autoOffDurationPref.get()

    suspend fun setAutoOffDuration(duration: AutoOffDuration) = autoOffDurationPref.set(duration)

    /** Null when no auto-off deadline is stored. */
    suspend fun autoOffDeadline(): Instant? =
        autoOffDeadlinePref.get()?.let { Instant.fromEpochMilliseconds(it) }

    suspend fun setAutoOffDeadline(at: Instant?) = autoOffDeadlinePref.set(at?.toEpochMilliseconds())
}
