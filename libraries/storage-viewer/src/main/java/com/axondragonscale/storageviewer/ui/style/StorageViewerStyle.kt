package com.axondragonscale.storageviewer.ui.style

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
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

// The dev-tools palette the style was drawn against; the DevTools theme supplies these same values
// at runtime, and the previews rebuild the scheme from them.
private val Background = Color(0xFF0E1013)
private val Surface = Color(0xFF171A1F)
private val SurfaceContainerHigh = Color(0xFF20242B)
private val OnSurface = Color(0xFFE6E9EE)
private val OnSurfaceVariant = Color(0xFF98A0AC)
private val Primary = Color(0xFF5FD3A6)
private val OnPrimary = Color(0xFF05231A)
private val PrimaryContainer = Color(0xFF1F3A30)
private val OnPrimaryContainer = Color(0xFFA5E7CC)
private val Outline = Color(0xFF313740)
private val OutlineVariant = Color(0xFF242931)
private val Scrim = Color(0xFF000000)

private val DefaultStyle = StorageViewerStyle(
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

internal val StorageViewerPreviewColorScheme: ColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceContainerHigh = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim,
)

internal val LocalStorageViewerStyle = staticCompositionLocalOf { DefaultStyle }

/** The viewer's defaults: the fixed dark palette, with no read of the ambient theme. */
@Composable
public fun rememberStorageViewerStyle(): StorageViewerStyle = DefaultStyle
