package com.axondragonscale.devinator.repository

import com.axondragonscale.commons.catching
import com.axondragonscale.commons.getOrDefault
import com.axondragonscale.devinator.model.Tweak
import com.axondragonscale.devinator.model.WriteResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Created by Ronak on 09/11/26 */

/**
 * The only writer of the Tweaks page, and the single observable truth about those keys.
 *
 * In character with [DevSettingsRepository], it keeps no state of its own: the map below is the last
 * read from `Settings.Global`, and one observer keeps the page honest when a value changes through
 * `adb` or Android's own Developer options screen.
 */
class DevTweaksRepository(
    private val settings: SettingsRepository,
) {

    /** Each key's table, so one observer can watch both stores. */
    private val stores = TweakCatalog.associate { it.key to it.store }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Reset is a cascade of writes; nothing may interleave with it. */
    private val resetMutex = Mutex()

    private val _values = MutableStateFlow(readAll())

    /** Every key's current value, read back from the database. */
    val values: StateFlow<Map<String, Float>> = _values.asStateFlow()

    init {
        scope.launch {
            settings.observeKeys(stores).collect { _values.value = readAll() }
        }
    }

    private fun readAll(): Map<String, Float> = TweakCatalog.associate { tweak ->
        tweak.key to (settings.read(tweak.store, tweak.key)?.toFloatOrNull() ?: tweak.stock)
    }

    suspend fun setToggle(tweak: Tweak.Toggle, enabled: Boolean): WriteResult =
        write { writeOne(tweak, if (enabled) 1f else 0f) }

    suspend fun select(tweak: Tweak, value: Float): WriteResult =
        write { writeOne(tweak, value) }

    /**
     * Writes the stock value for every row. A row that fails does not stop the rest, so one OEM
     * quirk cannot leave half the page reset.
     */
    suspend fun reset(): WriteResult = resetMutex.withLock {
        val wrote = TweakCatalog.map { tweak ->
            catching { writeOne(tweak, tweak.stock) }.getOrDefault(false)
        }
        if (wrote.all { it }) WriteResult.Ok else WriteResult.Failed
    }

    private fun writeOne(tweak: Tweak, value: Float): Boolean = when (tweak) {
        is Tweak.Toggle -> settings.writeInt(
            tweak.store,
            tweak.key,
            if (value != 0f) tweak.onValue else 0,
        )

        is Tweak.Limit -> settings.writeInt(tweak.store, tweak.key, value.toInt())
        is Tweak.Scale -> settings.writeFloat(tweak.store, tweak.key, value)
        is Tweak.FontScale -> settings.writeFloat(tweak.store, tweak.key, value)
    }

    /** A rejected write is a result, not a crash; the observer decides what the page shows. */
    private fun write(block: () -> Boolean): WriteResult =
        catching { if (block()) WriteResult.Ok else WriteResult.Failed }
            .getOrDefault(WriteResult.Failed)
}
