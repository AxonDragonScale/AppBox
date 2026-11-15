package com.axondragonscale.verso.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val MaterialTheme.verso: VersoColors
    @Composable
    @ReadOnlyComposable
    get() = LocalVersoColors.current

/** Square everywhere except the top edge of a sheet. */
val VersoShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(topStart = Dimens.sheetRadius, topEnd = Dimens.sheetRadius),
)

@Composable
fun VersoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) NightVersoColors else LightVersoColors

    CompositionLocalProvider(LocalVersoColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = VersoTypography,
            shapes = VersoShapes,
            content = content,
        )
    }
}

private fun VersoColors.toColorScheme() = if (isLight) {
    lightColorScheme().copy(
        primary = ink,
        onPrimary = paper,
        secondary = inkMuted,
        onSecondary = paper,
        background = paper,
        onBackground = ink,
        surface = paper,
        onSurface = ink,
        surfaceVariant = paperSunk,
        onSurfaceVariant = inkMuted,
        outline = rule,
        outlineVariant = rule,
        error = error,
        onError = Color.White,
        scrim = Color.Black,
    )
} else {
    darkColorScheme().copy(
        primary = ink,
        onPrimary = paper,
        secondary = inkMuted,
        onSecondary = paper,
        background = paper,
        onBackground = ink,
        surface = paper,
        onSurface = ink,
        surfaceVariant = paperSunk,
        onSurfaceVariant = inkMuted,
        outline = rule,
        outlineVariant = rule,
        error = error,
        onError = Color.Black,
        scrim = Color.Black,
    )
}
