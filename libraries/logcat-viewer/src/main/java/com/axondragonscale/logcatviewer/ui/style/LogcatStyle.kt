package com.axondragonscale.logcatviewer.ui.style

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.axondragonscale.logcatviewer.model.LogLevel

/** Created by Ronak on 09/11/26 */

/**
 * The few things the viewer needs that Material has no role for: the mono face for log lines, two
 * step-action sizes, and the level ramp for debug, warning and the trace block. A host can pass its
 * own to match the surrounding app.
 */
@Immutable
data class LogcatStyle(
    val mono: TextStyle,
    val action: TextStyle,
    val sectionTitle: TextStyle,
    val debug: Color,
    val warn: Color,
    val warnSurface: Color,
    val warnRowTint: Color,
    val errorRowTint: Color,
    val traceSurface: Color,
)

private val LevelDebug = Color(0xFF8FB8D8)
private val LevelWarn = Color(0xFFE8C468)

// The dev-tools palette the defaults were drawn against; the DevTools theme supplies these same
// values at runtime, and the previews rebuild the scheme from them.
private val Background = Color(0xFF0E1013)
private val Surface = Color(0xFF171A1F)
private val SurfaceVariant = Color(0xFF20242B)
private val OnSurface = Color(0xFFE6E9EE)
private val OnSurfaceVariant = Color(0xFF98A0AC)
private val Primary = Color(0xFF5FD3A6)
private val OnPrimary = Color(0xFF05231A)
private val PrimaryContainer = Color(0xFF1F3A30)
private val OnPrimaryContainer = Color(0xFFA5E7CC)
private val Error = Color(0xFFF2B8B5)
private val ErrorContainer = Color(0xFF3A1414)
private val Outline = Color(0xFF313740)

private val DefaultStyle = LogcatStyle(
    mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    action = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    sectionTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    debug = LevelDebug,
    warn = LevelWarn,
    warnSurface = LevelWarn.copy(alpha = 0.18f),
    warnRowTint = LevelWarn.copy(alpha = 0.10f),
    errorRowTint = Error.copy(alpha = 0.12f),
    traceSurface = Error.copy(alpha = 0.14f),
)

internal val LogcatPreviewColorScheme: ColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    errorContainer = ErrorContainer,
    outline = Outline,
)

internal val LocalLogcatStyle = staticCompositionLocalOf { DefaultStyle }

/** The viewer's defaults: the fixed dark ramp, with no read of the ambient theme. */
@Composable
fun rememberLogcatStyle(): LogcatStyle = remember { DefaultStyle }

/** The one hairline the screen, the menus and the sheets share between sections. */
@Composable
internal fun hairlineColor(): Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

@Composable
internal fun LogcatDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = hairlineColor())
}

/** A level's glyph, badge and row tint. One switch, because they always change together. */
@Immutable
internal data class LogLevelColors(val glyph: Color, val badge: Color, val row: Color)

@Composable
internal fun LogLevel.colors(): LogLevelColors {
    val style = LocalLogcatStyle.current
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        LogLevel.VERBOSE -> LogLevelColors(scheme.onSurfaceVariant, scheme.surfaceVariant, Color.Transparent)
        LogLevel.DEBUG -> LogLevelColors(style.debug, scheme.surfaceVariant, Color.Transparent)
        LogLevel.INFO -> LogLevelColors(scheme.primary, scheme.surfaceVariant, Color.Transparent)
        LogLevel.WARN -> LogLevelColors(style.warn, style.warnSurface, style.warnRowTint)
        LogLevel.ERROR, LogLevel.FATAL -> LogLevelColors(scheme.error, scheme.errorContainer, style.errorRowTint)
    }
}
