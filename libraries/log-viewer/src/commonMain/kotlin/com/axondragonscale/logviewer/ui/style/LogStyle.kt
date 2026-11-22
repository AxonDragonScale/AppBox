package com.axondragonscale.logviewer.ui.style

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
 * action sizes, and the level ramp for debug, warning and the trace block. The viewer is a library
 * with one look, so there is no theme seam for a host to reach through.
 */
internal object LogStyle {

    val mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    )

    val action = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )

    val sectionTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    )

    val debug = Color(0xFF8FB8D8)
    val warn = Color(0xFFE8C468)
    val warnSurface = warn.copy(alpha = 0.18f)
    val warnRowTint = warn.copy(alpha = 0.10f)
    val errorRowTint = Color(0xFFF2B8B5).copy(alpha = 0.12f)
    val traceSurface = Color(0xFFF2B8B5).copy(alpha = 0.14f)
}

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
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        LogLevel.TRACE -> LogLevelColors(scheme.onSurfaceVariant, scheme.surfaceVariant, Color.Transparent)
        LogLevel.DEBUG -> LogLevelColors(LogStyle.debug, scheme.surfaceVariant, Color.Transparent)
        LogLevel.INFO -> LogLevelColors(scheme.primary, scheme.surfaceVariant, Color.Transparent)
        LogLevel.WARN -> LogLevelColors(LogStyle.warn, LogStyle.warnSurface, LogStyle.warnRowTint)
        LogLevel.ERROR, LogLevel.FATAL -> LogLevelColors(scheme.error, scheme.errorContainer, LogStyle.errorRowTint)
    }
}
