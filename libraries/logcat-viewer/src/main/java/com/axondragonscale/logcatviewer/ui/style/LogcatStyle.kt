package com.axondragonscale.logcatviewer.ui.style

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    debug = Color(0xFF3A6B8A),
    warn = Color(0xFF7A5000),
    warnSurface = Color(0x1F7A5000),
    warnRowTint = Color(0x0F7A5000),
    errorRowTint = Color(0x0FBA1A1A),
    traceSurface = Color(0x14BA1A1A),
)

internal val LocalLogcatStyle = staticCompositionLocalOf { DefaultStyle }

/** The viewer's default: Material colours plus the app's neutral level ramp. */
@Composable
fun rememberLogcatStyle(): LogcatStyle {
    val scheme = MaterialTheme.colorScheme
    // The host may force Light/Dark, so the ramp follows the resolved scheme, not the system.
    val dark = scheme.background.luminance() < 0.5f
    return remember(dark, scheme) {
        val warn = if (dark) Color(0xFFE8C468) else DefaultStyle.warn
        DefaultStyle.copy(
            debug = if (dark) Color(0xFF8FB8D8) else DefaultStyle.debug,
            warn = warn,
            warnSurface = warn.copy(alpha = if (dark) 0.18f else 0.12f),
            warnRowTint = warn.copy(alpha = if (dark) 0.10f else 0.06f),
            errorRowTint = scheme.error.copy(alpha = if (dark) 0.12f else 0.06f),
            traceSurface = scheme.error.copy(alpha = if (dark) 0.14f else 0.08f),
        )
    }
}

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
