package com.axondragonscale.commons

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * A wrapper that encapsulates a DataStore preference with its read [flow] and [set] function.
 */
class PreferenceSetting<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val default: T,
) {
    val flow: Flow<T> = dataStore.data.map { it[key] ?: default }

    suspend fun get(): T = flow.first()

    suspend fun set(value: T) {
        dataStore.edit { it[key] = value }
    }
}

/**
 * A wrapper for enum preferences stored as strings.
 */
class EnumPreferenceSetting<E : Enum<E>>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    private val default: E,
    private val valueOf: (String) -> E,
) {
    val flow: Flow<E> = dataStore.data.map { prefs ->
        prefs[key]?.let(valueOf) ?: default
    }

    suspend fun get(): E = flow.first()

    suspend fun set(value: E) {
        dataStore.edit { it[key] = value.name }
    }
}

/**
 * Creates a [PreferenceSetting] for the given key and default value.
 */
fun <T> DataStore<Preferences>.setting(
    key: Preferences.Key<T>,
    default: T,
) = PreferenceSetting(this, key, default)

/**
 * Creates an [EnumPreferenceSetting] for the given key and default enum value.
 * The enum is stored as its name string.
 */
inline fun <reified E : Enum<E>> DataStore<Preferences>.enumSetting(
    key: Preferences.Key<String>,
    default: E,
) = EnumPreferenceSetting(this, key, default, ::enumValueOf)

/**
 * A wrapper for serializable model class preferences stored as JSON strings.
 * The model class must be annotated with @Serializable.
 */
class SerializedPreferenceSetting<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    private val default: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T,
) {
    val flow: Flow<T> = dataStore.data.map { prefs ->
        prefs[key]?.let { deserializer(it) } ?: default
    }

    suspend fun get(): T = flow.first()

    suspend fun set(value: T) {
        dataStore.edit { it[key] = serializer(value) }
    }
}

/**
 * Creates a [SerializedPreferenceSetting] for the given key and default value.
 * The model class must be annotated with @Serializable.
 *
 * @param key The preference key name
 * @param default The default value when no value is stored
 * @param json The Json instance to use for serialization/deserialization
 */
inline fun <reified T> DataStore<Preferences>.serializedSetting(
    key: String,
    default: T,
    json: Json = Json,
): SerializedPreferenceSetting<T> = SerializedPreferenceSetting(
    dataStore = this,
    key = stringPreferencesKey(key),
    default = default,
    serializer = { json.encodeToString(it) },
    deserializer = { json.decodeFromString(it) },
)
