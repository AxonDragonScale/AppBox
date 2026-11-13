package com.axondragonscale.commons

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Reads never throw at their caller: a store that fails to read is treated as empty, and a stored
 * value that fails to decode — a renamed enum constant, JSON written by an older app version —
 * falls back to [default]. Both cases are logged, so every wrapper below always answers a value.
 *
 * Only what a decoder can reject is caught here: a raw `Pref<T>`/`NullablePref<T>` whose stored
 * value has a different runtime type than its key promises still fails at the use site, because its
 * generic type is erased before this point.
 */
private inline fun <T> DataStore<Preferences>.read(
    default: T,
    crossinline decode: (Preferences) -> T,
): Flow<T> = data
    .catch { e ->
        this@read.log { e }
        emit(emptyPreferences())
    }
    .map { prefs ->
        // Decoding is not suspending, so swallowing everything here cannot hide cancellation.
        this@read.catching { decode(prefs) }.getOrElse { default }
    }

/**
 * A write that cannot take the app down; the caller has no useful recovery either way.
 * Cancellation still propagates, so a stalled scope is never reported as a successful write.
 */
private suspend fun DataStore<Preferences>.editQuietly(block: (MutablePreferences) -> Unit) {
    catching { edit(block) }
}

/**
 * A wrapper that encapsulates a DataStore preference with its read [flow] and [set] function.
 */
class Pref<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val default: T,
) {
    val flow: Flow<T> = dataStore.read(default) { prefs -> prefs[key] ?: default }

    suspend fun get(): T = flow.first()

    suspend fun set(value: T) = dataStore.editQuietly { it[key] = value }
}

/**
 * A wrapper for enum preferences stored as strings.
 */
class EnumPref<E : Enum<E>>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    private val default: E,
    private val valueOf: (String) -> E,
) {
    val flow: Flow<E> = dataStore.read(default) { prefs -> prefs[key]?.let(valueOf) ?: default }

    suspend fun get(): E = flow.first()

    suspend fun set(value: E) = dataStore.editQuietly { it[key] = value.name }
}

/**
 * A wrapper for serializable model class preferences stored as JSON strings.
 * The model class must be annotated with @Serializable.
 */
class SerializedPref<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    private val default: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
) {
    val flow: Flow<T> = dataStore.read(default) { prefs -> prefs[key]?.let(deserializer) ?: default }

    suspend fun get(): T = flow.first()

    suspend fun set(value: T) = dataStore.editQuietly { it[key] = serializer(value) }
}

/**
 * A preference that may be **absent**. [get] answers null when nothing is stored, and [set] removes
 * the key for a null value — use this instead of encoding null as a sentinel inside a [Pref].
 */
class NullablePref<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
) {
    val flow: Flow<T?> = dataStore.read<T?>(null) { prefs -> prefs[key] }

    suspend fun get(): T? = flow.first()

    suspend fun set(value: T?) = dataStore.editQuietly { prefs ->
        if (value == null) prefs.remove(key) else prefs[key] = value
    }
}

/**
 * Creates a [NullablePref] for the given key. Nothing stored means null; there is no default.
 */
fun <T> DataStore<Preferences>.nullablePref(
    key: Preferences.Key<T>,
) = NullablePref(this, key)

/**
 * Creates a [Pref] for the given key and default value.
 */
fun <T> DataStore<Preferences>.pref(
    key: Preferences.Key<T>,
    default: T,
) = Pref(this, key, default)

/**
 * Creates an [EnumPref] for the given key and default enum value.
 * The enum is stored as its name string.
 */
inline fun <reified E : Enum<E>> DataStore<Preferences>.enumPref(
    key: Preferences.Key<String>,
    default: E,
) = EnumPref(this, key, default, ::enumValueOf)

/**
 * Creates a [SerializedPref] for the given key and default value.
 * The model class must be annotated with @Serializable.
 *
 * @param key The preference key name
 * @param default The default value when no value is stored
 * @param json The Json instance to use for serialization/deserialization
 */
inline fun <reified T> DataStore<Preferences>.serializedPref(
    key: String,
    default: T,
    json: Json = Json,
): SerializedPref<T> = SerializedPref(
    dataStore = this,
    key = stringPreferencesKey(key),
    default = default,
    serializer = { json.encodeToString(it) },
    deserializer = { json.decodeFromString(it) },
)
