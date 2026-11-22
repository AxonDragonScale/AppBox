package com.axondragonscale.commons.devtools.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The one scheme every developer tool renders in: a neutral dark palette, never the host app's
 * colours, type scale or shapes.
 */
public val DevToolsColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF5FD3A6),
    onPrimary = Color(0xFF05231A),
    primaryContainer = Color(0xFF1F3A30),
    onPrimaryContainer = Color(0xFFA5E7CC),
    inversePrimary = Color(0xFF1F3A30),

    secondary = Color(0xFFC3CAD4),
    onSecondary = Color(0xFF1B1F24),
    secondaryContainer = Color(0xFF262B33),
    onSecondaryContainer = Color(0xFFD3DAE3),

    tertiary = Color(0xFFE8C468),
    onTertiary = Color(0xFF2A2000),
    tertiaryContainer = Color(0xFF3A2F12),
    onTertiaryContainer = Color(0xFFE8C468),

    background = Color(0xFF0E1013),
    onBackground = Color(0xFFE6E9EE),
    surface = Color(0xFF171A1F),
    onSurface = Color(0xFFE6E9EE),
    surfaceVariant = Color(0xFF20242B),
    onSurfaceVariant = Color(0xFF98A0AC),
    surfaceTint = Color(0xFF5FD3A6),
    inverseSurface = Color(0xFFE6E9EE),
    inverseOnSurface = Color(0xFF171A1F),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF3A1414),
    errorContainer = Color(0xFF3A1414),
    onErrorContainer = Color(0xFFF2B8B5),

    outline = Color(0xFF313740),
    outlineVariant = Color(0xFF242931),
    scrim = Color(0xFF000000),

    surfaceDim = Color(0xFF0E1013),
    surfaceBright = Color(0xFF2A2F37),
    surfaceContainerLowest = Color(0xFF090B0D),
    surfaceContainerLow = Color(0xFF12151A),
    surfaceContainer = Color(0xFF1A1E24),
    surfaceContainerHigh = Color(0xFF20242B),
    surfaceContainerHighest = Color(0xFF262B33),
)

/**
 * Pins a developer tool to the fixed dark [DevToolsColorScheme], so nothing in it follows the host
 * app's `MaterialTheme` — dynamic colour, light/dark choice, type scale or shape overrides.
 */
@Composable
public fun DevToolsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DevToolsColorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content,
    )
}
