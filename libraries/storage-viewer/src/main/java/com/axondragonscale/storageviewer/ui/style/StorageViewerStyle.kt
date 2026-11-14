package com.axondragonscale.storageviewer.ui.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Created by Ronak on 14/11/26 */

/**
 * The parts of the viewer Material has no role for: the mono faces the data surfaces read in, the
 * three store-kind hues, the value-kind labels and the fills behind chips and skeletons. A host can
 * pass its own to sit the viewer inside its palette.
 */
@Immutable
public data class StorageViewerStyle(
    val monoKey: TextStyle,
    val monoValue: TextStyle,
    val monoIdentifier: TextStyle,
    val monoChip: TextStyle,
    val storeDatabase: Color,
    val storePreferences: Color,
    val typeFiles: Color,
    val kindNeutral: Color,
    val kindNumber: Color,
    val kindBool: Color,
    val chipFill: Color,
    val iconTileFill: Color,
    val skeletonFill: Color,
    val warn: Color,
    val warnSurface: Color,
    val errorSurface: Color,
)

private fun mono(size: Float, weight: FontWeight): TextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.35f).sp,
    letterSpacing = 0.sp,
)

private fun viewerStyle(dark: Boolean): StorageViewerStyle = if (dark) {
    StorageViewerStyle(
        monoKey = mono(12f, FontWeight.Normal),
        monoValue = mono(13f, FontWeight.Normal),
        monoIdentifier = mono(15f, FontWeight.Medium),
        monoChip = mono(11f, FontWeight.Normal),
        storeDatabase = Color(0xFF8FB8D8),
        storePreferences = Color(0xFFC7A6F5),
        typeFiles = Color(0xFF6FC7C9),
        kindNeutral = Color(0xFFC3CAD4),
        kindNumber = Color(0xFF8FB8D8),
        kindBool = Color(0xFFE8C468),
        chipFill = Color(0xFF20242B),
        iconTileFill = Color(0xFF20242B),
        skeletonFill = Color(0xFF282D34),
        warn = Color(0xFFE8C468),
        warnSurface = Color(0xFF2B2214),
        errorSurface = Color(0xFF3A1414),
    )
} else {
    StorageViewerStyle(
        monoKey = mono(12f, FontWeight.Normal),
        monoValue = mono(13f, FontWeight.Normal),
        monoIdentifier = mono(15f, FontWeight.Medium),
        monoChip = mono(11f, FontWeight.Normal),
        storeDatabase = Color(0xFF3A6B8A),
        storePreferences = Color(0xFF6B4A9C),
        typeFiles = Color(0xFF0F6B6E),
        kindNeutral = Color(0xFF3F4854),
        kindNumber = Color(0xFF3A6B8A),
        kindBool = Color(0xFF7A5000),
        chipFill = Color(0xFFEEF1F5),
        iconTileFill = Color(0xFFEEF1F5),
        skeletonFill = Color(0xFFE4E8EE),
        warn = Color(0xFF7A5000),
        warnSurface = Color(0xFFF6EFDC),
        errorSurface = Color(0xFFFCE4E2),
    )
}

internal val LocalStorageViewerStyle = staticCompositionLocalOf { viewerStyle(dark = true) }

/**
 * The viewer's default: Material colours plus the board's mono faces and kind ramp.
 */
@Composable
public fun rememberStorageViewerStyle(): StorageViewerStyle {
    // The palette follows the resolved scheme's luminance, so a host forcing Light or Dark gets a
    // matching viewer without threading a theme flag through every call site.
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return remember(dark) { viewerStyle(dark) }
}
