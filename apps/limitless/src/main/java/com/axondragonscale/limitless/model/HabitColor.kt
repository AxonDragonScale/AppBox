package com.axondragonscale.limitless.model

/**
 * A curated set of colors a user can assign to a habit.
 *
 * Colors are stored as ARGB [Long] values so this model stays free of Compose dependencies.
 * Use the `HabitColor.color` Compose extension (in `ui/theme/Color.kt`) to get a [Color].
 */
enum class HabitColor(val argb: Long) {
    PURPLE(0xFF6650A4),
    PINK(0xFFD81B60),
    ROSE(0xFFEC407A),
    RED(0xFFD32F2F),
    ORANGE(0xFFEF6C00),
    YELLOW(0xFFF9A825),
    LIME(0xFF7CB342),
    GREEN(0xFF2E7D32),
    TEAL(0xFF00897B),
    BLUE(0xFF1565C0),
    INDIGO(0xFF283593),
    CYAN(0xFF00838F),
    ;

    companion object {
        fun fromName(name: String): HabitColor =
            entries.firstOrNull { it.name == name } ?: PURPLE
    }
}
