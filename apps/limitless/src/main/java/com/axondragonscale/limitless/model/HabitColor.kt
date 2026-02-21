package com.axondragonscale.limitless.model

/**
 * A curated set of colors a user can assign to a habit.
 *
 * Colors are stored as ARGB [Long] values so this model stays free of Compose dependencies.
 * Use the `HabitColor.color` Compose extension (in `ui/theme/Color.kt`) to get a [Color].
 *
 * Each color is chosen to work well as a `seedColor` for MaterialKolor palette generation —
 * moderately saturated, mid-range lightness, and evenly distributed across the hue wheel.
 */
enum class HabitColor(val argb: Long) {
    RED(0xFFD32F2F),
    ROSE(0xFFD81B60),
    PINK(0xFFEC407A),
    FUCHSIA(0xFFAB47BC),
    PURPLE(0xFF7E57C2),
    VIOLET(0xFF5C6BC0),
    INDIGO(0xFF3949AB),
    BLUE(0xFF1E88E5),
    SKY(0xFF039BE5),
    CYAN(0xFF00ACC1),
    TEAL(0xFF00897B),
    GREEN(0xFF43A047),
    LIME(0xFF7CB342),
    YELLOW(0xFFF9A825),
    AMBER(0xFFFFB300),
    ORANGE(0xFFEF6C00),
    VERMILION(0xFFF4511E),
    BROWN(0xFF8D6E63),
    ;

    companion object {
        fun fromName(name: String): HabitColor =
            entries.firstOrNull { it.name == name } ?: PURPLE
    }
}
