package com.axondragonscale.devinator.model

/** Created by Ronak on 09/11/26 */

/** The groups the Tweaks page draws, in this order. */
enum class TweakGroup(val label: String) {
    DISPLAY("Display"),
    ANIMATION("Animation"),
    BEHAVIOUR("Behaviour"),
}

/** The glyph a row shows. The model names the intent; the UI owns the actual vector. */
enum class TweakIcon {
    FONT_SIZE,
    ANIMATION,
    LIFECYCLE,
    PROCESSES,
    ANR,
    AWAKE,
}

/**
 * Which settings table a key lives in. Most rows are `Global`; `System` is where the input overlays
 * and the font scale live. Writing the wrong table is a silent no-op (or, for `System`'s private
 * keys, a hard refusal).
 */
enum class SettingsStore { GLOBAL, SYSTEM }

/**
 * One row on the Tweaks page.
 *
 * Every key is a number, so one [Float] covers every shape. A [Toggle] stores [Toggle.onValue]
 * instead of 1 because "stay awake while charging" is a plug-type bitmask.
 *
 * Nothing here is persisted: the value is always read back from the settings database.
 */
sealed interface Tweak {
    val key: String
    val group: TweakGroup
    val title: String
    val caption: String
    val icon: TweakIcon

    /** The table the key lives in; writing the wrong one silently does nothing. */
    val store: SettingsStore

    /** The stock value Reset writes back. */
    val stock: Float

    /** The values a choice row offers; empty for a toggle. */
    val choices: List<Float>

    /** A 0/1 switch, stored as an int. */
    data class Toggle(
        override val key: String,
        override val group: TweakGroup,
        override val title: String,
        override val caption: String,
        override val icon: TweakIcon,
        override val store: SettingsStore = SettingsStore.GLOBAL,
        val onValue: Int = 1,
    ) : Tweak {
        override val stock: Float get() = 0f
        override val choices: List<Float> get() = emptyList()
    }

    /** An animation scale: Off, 0.5×, 1×, 1.5×, 2×, 5×, 10×. Stock is 1×. */
    data class Scale(
        override val key: String,
        override val group: TweakGroup,
        override val title: String,
        override val caption: String,
        override val icon: TweakIcon,
        override val store: SettingsStore = SettingsStore.GLOBAL,
    ) : Tweak {
        override val stock: Float get() = 1f
        override val choices: List<Float> get() = SCALE_CHOICES

        companion object {
            val SCALE_CHOICES = listOf(0f, 0.5f, 1f, 1.5f, 2f, 5f, 10f)
        }
    }

    /**
     * The system font scale, `Settings.System.font_scale`. Not a developer option: writing it needs
     * `WRITE_SETTINGS`, and the platform applies it to every app live. Stock is 1×.
     */
    data class FontScale(
        override val key: String,
        override val group: TweakGroup,
        override val title: String,
        override val caption: String,
        override val icon: TweakIcon,
        override val store: SettingsStore = SettingsStore.SYSTEM,
    ) : Tweak {
        override val stock: Float get() = 1f
        override val choices: List<Float> get() = FONT_CHOICES

        companion object {
            val FONT_CHOICES = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f, 2f)
        }
    }

    /** The background process limit; -1 means Standard. */
    data class Limit(
        override val key: String,
        override val group: TweakGroup,
        override val title: String,
        override val caption: String,
        override val icon: TweakIcon,
        override val store: SettingsStore = SettingsStore.GLOBAL,
    ) : Tweak {
        override val stock: Float get() = -1f
        override val choices: List<Float> get() = LIMIT_CHOICES

        companion object {
            val LIMIT_CHOICES = listOf(-1f, 0f, 1f, 2f, 3f, 4f)
        }
    }
}
