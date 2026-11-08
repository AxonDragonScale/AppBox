package com.axondragonscale.devinator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Created by Ronak on 11/06/26 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkPrimary,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightPrimary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

/** The hero card's tints, which have no Material role of their own. */
@Immutable
data class DevinatorColors(
    val primarySurface: Color,
    val primaryOutline: Color,
    val errorSurfaceHigh: Color,
    val errorOutline: Color,
)

private val DarkDevinatorColors = DevinatorColors(
    primarySurface = DarkPrimarySurface,
    primaryOutline = DarkPrimaryOutline,
    errorSurfaceHigh = DarkErrorSurfaceHigh,
    errorOutline = DarkErrorOutline,
)

private val LightDevinatorColors = DevinatorColors(
    primarySurface = LightPrimarySurface,
    primaryOutline = LightPrimaryOutline,
    errorSurfaceHigh = LightErrorSurfaceHigh,
    errorOutline = LightErrorOutline,
)

val LocalDevinatorColors = staticCompositionLocalOf { DarkDevinatorColors }

/**
 * The app ships its own palette by default. Settings can switch on dynamic colour (Material You) and
 * pick Light / Dark / System; previews pin the fallback palette so contrast is reviewed against what
 * a device without a wallpaper theme actually shows.
 */
@Composable
fun DevinatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            val scheme = if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            // Material You gives `surface` the same tone as `background`, which flattens every card
            // into the page. Step the card and inset tones so the layout still reads.
            scheme.copy(
                surface = if (darkTheme) scheme.surfaceContainer else scheme.surfaceContainerLowest,
                surfaceVariant = scheme.surfaceContainerHigh,
            )
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.isNavigationBarContrastEnforced = false
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DevinatorTypography,
    ) {
        CompositionLocalProvider(
            LocalDevinatorColors provides if (darkTheme) DarkDevinatorColors else LightDevinatorColors,
            content = content,
        )
    }
}
