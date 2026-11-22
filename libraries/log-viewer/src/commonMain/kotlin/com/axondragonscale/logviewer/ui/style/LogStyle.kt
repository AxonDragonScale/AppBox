package com.axondragonscale.logviewer.ui.style

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.axondragonscale.logviewer.model.LogLevel

/** Created by Ronak Harkhani on 11/22/26 */

/**
 * The few things the viewer needs that Material has no role for: the mono face for log lines, two
 * action sizes, and the level ramp for debug, warning and the trace block.
 */
@Immutable
data class LogStyle(
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
private val ErrorTint = Color(0xFFF2B8B5)

private val DefaultStyle = LogStyle(
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
    errorRowTint = ErrorTint.copy(alpha = 0.12f),
    traceSurface = ErrorTint.copy(alpha = 0.14f),
)

internal val LocalLogStyle = staticCompositionLocalOf { DefaultStyle }

@Composable
internal fun hairlineColor(): Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

@Composable
internal fun LogDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = hairlineColor())
}

/** A level's glyph, badge and row tint. One switch, because they always change together. */
@Immutable
internal data class LogLevelColors(val glyph: Color, val badge: Color, val row: Color)

@Composable
internal fun LogLevel.colors(): LogLevelColors {
    val style = LocalLogStyle.current
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        LogLevel.TRACE -> LogLevelColors(scheme.onSurfaceVariant, scheme.surfaceVariant, Color.Transparent)
        LogLevel.DEBUG -> LogLevelColors(style.debug, scheme.surfaceVariant, Color.Transparent)
        LogLevel.INFO -> LogLevelColors(scheme.primary, scheme.surfaceVariant, Color.Transparent)
        LogLevel.WARN -> LogLevelColors(style.warn, style.warnSurface, style.warnRowTint)
        LogLevel.ERROR, LogLevel.FATAL -> LogLevelColors(scheme.error, scheme.errorContainer, style.errorRowTint)
    }
}
