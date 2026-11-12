package com.axondragonscale.wallmatic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Created by Ronak Harkhani on 06/06/24
 */

val MaterialTheme.wallmatic: WallmaticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalWallmaticColors.current

@Composable
fun WallmaticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val base = if (darkTheme) DarkWallmaticColors else LightWallmaticColors

    // Dynamic colour only supplies the accent; paper, ink and rules stay part of the identity.
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamic = if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
        base.copy(
            primary = dynamic.primary,
            onPrimary = dynamic.onPrimary,
            primaryContainer = dynamic.primaryContainer,
            onPrimaryContainer = dynamic.onPrimaryContainer,
        )
    } else {
        base
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalWallmaticColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            typography = Typography,
            shapes = SquareShapes,
            content = content,
        )
    }
}

private fun WallmaticColors.toColorScheme(dark: Boolean): ColorScheme {
    val scheme = if (dark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = ink,
        onSecondary = paper,
        secondaryContainer = paperHigh,
        onSecondaryContainer = ink,
        tertiary = inkMuted,
        onTertiary = paper,
        tertiaryContainer = paperHigh,
        onTertiaryContainer = ink,
        background = paper,
        onBackground = ink,
        surface = paper,
        onSurface = ink,
        surfaceVariant = paperLow,
        onSurfaceVariant = inkMuted,
        surfaceTint = primary,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = paperHigh,
        onErrorContainer = error,
        outline = ruleSoft,
        outlineVariant = ruleSoft,
        scrim = Color.Black,
        surfaceBright = paperWhite,
        surfaceDim = paperLow,
        surfaceContainerLowest = paperWhite,
        surfaceContainerLow = paper,
        surfaceContainer = paperLow,
        surfaceContainerHigh = paperHigh,
        surfaceContainerHighest = paperHigh,
    )
}
