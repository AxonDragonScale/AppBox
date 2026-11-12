package com.axondragonscale.wallmatic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Created by Ronak Harkhani on 06/06/24
 */

@Immutable
data class WallmaticColors(
    val paper: Color,
    val paperLow: Color,
    val paperHigh: Color,
    val paperWhite: Color,
    val ink: Color,
    val inkMuted: Color,
    val ruleSoft: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val error: Color,
    val onError: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val scrim: Color,
)

val LightWallmaticColors = WallmaticColors(
    paper = Color(0xFFF2EFE6),
    paperLow = Color(0xFFE9E4D8),
    paperHigh = Color(0xFFDCD6C8),
    paperWhite = Color(0xFFFFFFFF),
    ink = Color(0xFF141414),
    inkMuted = Color(0xFF6B655C),
    ruleSoft = Color(0xFFC9C2B4),
    primary = Color(0xFF4B46C0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCD9F5),
    onPrimaryContainer = Color(0xFF141235),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    inverseSurface = Color(0xFF141414),
    inverseOnSurface = Color(0xFFF2EFE6),
    scrim = Color.Black.copy(alpha = 0.3f),
)

val DarkWallmaticColors = WallmaticColors(
    paper = Color(0xFF14120F),
    paperLow = Color(0xFF1C1916),
    paperHigh = Color(0xFF2A2621),
    paperWhite = Color(0xFF1C1916),
    ink = Color(0xFFF2EFE6),
    inkMuted = Color(0xFFA7A29A),
    ruleSoft = Color(0xFF3A352E),
    primary = Color(0xFFC9C5FF),
    onPrimary = Color(0xFF1A1740),
    primaryContainer = Color(0xFF2A2650),
    onPrimaryContainer = Color(0xFFE4E1FF),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    inverseSurface = Color(0xFFF2EFE6),
    inverseOnSurface = Color(0xFF141414),
    scrim = Color.Black.copy(alpha = 0.6f),
)

val LocalWallmaticColors = staticCompositionLocalOf { LightWallmaticColors }
