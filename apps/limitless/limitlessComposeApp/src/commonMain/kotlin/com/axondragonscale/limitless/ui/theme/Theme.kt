package com.axondragonscale.limitless.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

/**
 * Created by Ronak Harkhani on 01/11/24
 *
 * Common [LimitlessTheme] composable.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param colorScheme Optional override — Android passes Material You dynamic colors here.
 *                    Desktop and iOS omit this and get the static MaterialKolor scheme.
 * @param content The composable content.
 */
@Composable
fun LimitlessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val resolvedScheme = colorScheme
        ?: rememberDynamicColorScheme(Color.Black, darkTheme, style = PaletteStyle.Monochrome)

    MaterialTheme(
        colorScheme = resolvedScheme,
        typography = kodchasanTypography(),
        content = content,
    )
}
