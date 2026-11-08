package com.axondragonscale.devinator.repository

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.axondragonscale.devinator.model.DevState
import com.axondragonscale.devinator.model.SettingsStore
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

/**
 * The only code that touches the settings provider.
 *
 * The three flags live in `Settings.Global`. A Tweaks row names its own table: most are `Global`,
 * but `show_touches` and `pointer_location` are `Settings.System` keys — that is where both AOSP's
 * `InputManagerService` and this ROM read them, and writing `Global` for them does nothing.
 */
class SettingsRepository(context: Context) {

    private val resolver: ContentResolver = context.contentResolver

    fun readState(): DevState = DevState(
        developerOptions = readFlag(KEY_DEVELOPER_OPTIONS),
        usbDebugging = readFlag(KEY_USB_DEBUGGING),
        wirelessDebugging = readFlag(KEY_WIRELESS_DEBUGGING),
    )

    fun readFlag(key: String): Boolean =
        parseFlag(Settings.Global.getString(resolver, key))

    /** Returns false when the provider rejected the write. Throws [SecurityException] without the grant. */
    fun writeFlag(key: String, enabled: Boolean): Boolean =
        Settings.Global.putInt(resolver, key, if (enabled) 1 else 0)

    /**
     * The raw stored value from [store], or null when the key was never set. Numeric reads go
     * through the string form for the same reason [parseFlag] does: `adb shell settings put` stores
     * text and `getFloat` throws on it.
     */
    fun read(store: SettingsStore, key: String): String? = when (store) {
        SettingsStore.GLOBAL -> Settings.Global.getString(resolver, key)
        SettingsStore.SYSTEM -> Settings.System.getString(resolver, key)
    }

    /** Stores an int: a 0/1 flag, or a fixed choice such as a process limit. */
    fun writeInt(store: SettingsStore, key: String, value: Int): Boolean = when (store) {
        SettingsStore.GLOBAL -> Settings.Global.putInt(resolver, key, value)
        SettingsStore.SYSTEM -> Settings.System.putInt(resolver, key, value)
    }

    /** Stores a float; an animation scale is the only shape that needs one. */
    fun writeFloat(store: SettingsStore, key: String, value: Float): Boolean = when (store) {
        SettingsStore.GLOBAL -> Settings.Global.putFloat(resolver, key, value)
        SettingsStore.SYSTEM -> Settings.System.putFloat(resolver, key, value)
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

    /**
     * Emits once, then again after any key in [keys] changes in its own table. The caller re-reads,
     * so a write made anywhere — `adb`, Android Settings, another screen — lands without a refresh.
     */
    fun observeKeys(keys: Map<String, SettingsStore>): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        keys.forEach { (key, store) ->
            val uri = when (store) {
                SettingsStore.GLOBAL -> Settings.Global.getUriFor(key)
                SettingsStore.SYSTEM -> Settings.System.getUriFor(key)
            }
            resolver.registerContentObserver(uri, false, observer)
        }
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }
}
