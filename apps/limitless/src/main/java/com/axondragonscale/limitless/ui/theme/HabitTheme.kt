package com.axondragonscale.limitless.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import com.axondragonscale.limitless.model.HabitColor
import com.materialkolor.rememberDynamicColorScheme

/**
 * Created by Ronak Harkhani on 22/02/26
 */

/**
 * A nested theme that generates a full Material 3 [ColorScheme] from a [HabitColor] seed.
 *
 * Designed to wrap habit-specific screens (`Habit`, `Editor`, `HabitDay`) so that all
 * standard M3 components automatically inherit the habit's color with correct tonal
 * palettes and contrast.
 *
 * Must be used inside [LimitlessTheme] — it preserves the parent typography (Kodchasan)
 * and derives dark/light mode from the base scheme's surface luminance.
 */
@Composable
fun HabitTheme(
    habitColor: HabitColor,
    content: @Composable () -> Unit,
) {
    val colorScheme = rememberDynamicColorScheme(
        seedColor = habitColor.color,
        isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
