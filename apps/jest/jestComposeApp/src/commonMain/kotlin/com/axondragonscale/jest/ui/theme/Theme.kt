package com.axondragonscale.jest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

internal val JestDarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkBackground,
    surfaceContainerHigh = DarkSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,

    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnBackground,

    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnBackground,

    error = DarkError,
    onError = DarkOnPrimary,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnBackground,

    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkTextSecondary,
)

internal val JestLightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    surfaceContainer = LightSurface,
    surfaceContainerLow = LightBackground,
    surfaceContainerHigh = LightSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,

    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnBackground,

    secondary = LightSecondary,
    onSecondary = LightOnPrimary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnBackground,

    error = LightError,
    onError = LightOnPrimary,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnBackground,

    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightTextSecondary,
)

/**
 * Common JestTheme composable.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param colorScheme Optional override — Android passes dynamic colors here.
 *                    Desktop and iOS omit this and get the static scheme.
 * @param content The composable content.
 */
@Composable
fun JestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val resolvedColorScheme = colorScheme
        ?: if (darkTheme) JestDarkColorScheme else JestLightColorScheme

    MaterialTheme(
        colorScheme = resolvedColorScheme,
        typography = JestTypography,
        content = content,
    )
}
