package com.axondragonscale.jest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
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
        ?: if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = resolvedColorScheme,
        typography = Typography,
        content = content,
    )
}
