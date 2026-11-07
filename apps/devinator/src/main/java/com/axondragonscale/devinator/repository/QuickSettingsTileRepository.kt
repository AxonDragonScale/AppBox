package com.axondragonscale.devinator.repository

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.axondragonscale.commons.log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/** Created by Ronak on 11/06/26 */

// AOSP keeps the tile list here, but refuses to read it above target SDK 33.
private const val COMMA_STORE = "sysui_qs_tiles"

// OriginOS keeps the same tiles in its own JSON grid instead, and leaves this key readable.
private const val JSON_STORE = "super_qs_tiles"

// Both stores name a tile the same way: `custom(package/class)`.
private val TILE_ENTRY = Regex("""custom\(([^)]+)\)""")

/**
 * Reads and edits the Quick Settings tile list.
 *
 * There is no public API to read or remove a tile, so this works through whichever secure setting
 * the device keeps. AOSP's key is readable only up to target SDK 33; OriginOS keeps its own,
 * readable and writable one. Removal edits the value it just read, so the user's other tiles
 * always survive.
 */
class QuickSettingsTileRepository(context: Context) {

    private val resolver: ContentResolver = context.contentResolver

    val addedClassNames: Flow<Set<String>> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(currentClassNames())
            }
        }
        STORES.forEach { resolver.registerContentObserver(Settings.Secure.getUriFor(it), false, observer) }
        trySend(currentClassNames())
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.distinctUntilChanged()

    fun currentClassNames(): Set<String> = readRaw()
        ?.let(::componentsIn)
        ?.mapNotNullTo(mutableSetOf()) { it.className }
        .orEmpty()

    /** Only for platforms without `StatusBarManager.requestAddTileService`; the JSON store needs
the system to insert, since its grid cannot be appended to as text. */
    fun add(component: ComponentName): Boolean {
        if (activeStore() != COMMA_STORE) return false
        val raw = rawValue(COMMA_STORE) ?: return false
        val tokens = raw.split(',').map(String::trim).filter(String::isNotEmpty)
        if (tokens.any { it.component() == component }) return true
        val entry = "custom(${component.flattenToShortString()})"
        return writeValue(COMMA_STORE, (tokens + entry).joinToString(","))
    }

    /** False when the store is unreadable, since a blind write would drop the user's other tiles. */
    fun remove(component: ComponentName): Boolean {
        val store = activeStore() ?: return false
        val raw = rawValue(store) ?: return false
        val updated = if (store == JSON_STORE) {
            removeFromJson(raw, component)
        } else {
            removeFromComma(raw, component)
        } ?: return false
        return updated == raw || writeValue(store, updated)
    }

    private fun activeStore(): String? = STORES.firstOrNull { rawValue(it) != null }

    private fun readRaw(): String? = activeStore()?.let(::rawValue)

    /**
     * A refused key and an empty one are the same answer here: the device keeps no tiles we can see,
     * so the page offers to add instead of removing blind.
     */
    private fun rawValue(key: String): String? = try {
        Settings.Secure.getString(resolver, key)
    } catch (e: Exception) {
        this.log { e }
        null
    }

    private fun writeValue(key: String, raw: String): Boolean = try {
        Settings.Secure.putString(resolver, key, raw)
        true
    } catch (e: Exception) {
        this.log { e }
        false
    }

    /** One scan reads either format, since both spell their tiles as `custom(package/class)`. */
    private fun componentsIn(raw: String): List<ComponentName> = TILE_ENTRY.findAll(raw)
        .mapNotNull { ComponentName.unflattenFromString(it.groupValues[1]) }
        .toList()

    private fun removeFromComma(raw: String, component: ComponentName): String = raw
        .split(',')
        .filterNot { it.trim().component() == component }
        .joinToString(",")

    private fun removeFromJson(raw: String, component: ComponentName): String? = try {
        val root = Json.parseToJsonElement(raw)
        val updated = root.withoutTile(component) ?: return null
        Json.encodeToString(JsonElement.serializer(), updated)
    } catch (e: Exception) {
        this.log { e }
        null
    }

    /** Null drops the whole node: it is the tile being removed. Everything else is kept as is. */
    private fun JsonElement.withoutTile(component: ComponentName): JsonElement? = when (this) {
        is JsonObject -> {
            if ((jsonObject["spec"] as? JsonPrimitive)?.component() == component) {
                null
            } else {
                JsonObject(
                    jsonObject.entries.mapNotNull { (key, value) ->
                        value.withoutTile(component)?.let { key to it }
                    }.toMap(),
                )
            }
        }

        is JsonArray -> JsonArray(jsonArray.mapNotNull { it.withoutTile(component) })
        else -> this
    }

    private fun String.component(): ComponentName? = TILE_ENTRY.matchEntire(trim())
        ?.let { ComponentName.unflattenFromString(it.groupValues[1]) }

    private fun JsonPrimitive.component(): ComponentName? = content.component()

    private companion object {
        val STORES = listOf(JSON_STORE, COMMA_STORE)
    }
}
