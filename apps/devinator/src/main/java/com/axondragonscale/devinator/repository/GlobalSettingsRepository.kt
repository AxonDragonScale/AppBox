package com.axondragonscale.devinator.repository

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.axondragonscale.devinator.model.DevState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Created by Ronak on 11/06/26 */

const val KEY_DEVELOPER_OPTIONS = Settings.Global.DEVELOPMENT_SETTINGS_ENABLED
const val KEY_USB_DEBUGGING = Settings.Global.ADB_ENABLED

// `Settings.Global.ADB_WIFI_ENABLED` is @hide since API 30; the literal is the only handle on it.
const val KEY_WIRELESS_DEBUGGING = "adb_wifi_enabled"

/**
 * The flags that follow developer options: turning it off zeroes them and remembers their values,
 * and turning it back on restores them. Add a new toggle key here and both sides pick it up.
 */
val RESTORABLE_FLAG_KEYS = listOf(KEY_USB_DEBUGGING, KEY_WIRELESS_DEBUGGING)

private val FLAG_KEYS = listOf(KEY_DEVELOPER_OPTIONS) + RESTORABLE_FLAG_KEYS

/**
 * Reads a flag value as the database stored it. `adb shell settings put` stores text while
 * `putInt` stores an integer, and `getInt` throws on the text form, so every read goes through
 * `getString`.
 */
fun parseFlag(raw: String?): Boolean = (raw?.toIntOrNull() ?: 0) != 0

/** The only code that touches `Settings.Global`; the repository above it owns what the values mean. */
class GlobalSettingsRepository(context: Context) {

    private val resolver: ContentResolver = context.contentResolver

    fun readState(): DevState = DevState(
        developerOptions = readFlag(KEY_DEVELOPER_OPTIONS),
        usbDebugging = readFlag(KEY_USB_DEBUGGING),
        wirelessDebugging = readFlag(KEY_WIRELESS_DEBUGGING),
    )

    fun readFlag(key: String): Boolean =
        parseFlag(Settings.Global.getString(resolver, key))

    /** Throws [SecurityException] when `WRITE_SECURE_SETTINGS` is missing. */
    fun writeFlag(key: String, enabled: Boolean) {
        Settings.Global.putInt(resolver, key, if (enabled) 1 else 0)
    }

    /**
     * The current flags, then again after any of the three keys changes. One observer covers all
     * of them, so a cascade that writes several keys just re-reads them.
     */
    fun observeState(): Flow<DevState> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(readState())
            }
        }
        FLAG_KEYS.forEach { key ->
            resolver.registerContentObserver(Settings.Global.getUriFor(key), false, observer)
        }
        trySend(readState())
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.distinctUntilChanged()
}
