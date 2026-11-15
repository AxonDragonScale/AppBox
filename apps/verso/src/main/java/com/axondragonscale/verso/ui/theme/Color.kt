package com.axondragonscale.verso.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app palette. Reading themes are a separate set that arrives with the reader; these are the
 * library, stats and settings colours.
 */
@Immutable
data class VersoColors(
    val paper: Color,
    val paperSunk: Color,
    val ink: Color,
    val inkMuted: Color,
    val rule: Color,
    val highlight: Color,
    val selection: Color,
    val error: Color,
    val isLight: Boolean,
)

val LightVersoColors = VersoColors(
    paper = Color(0xFFF7F4EC),
    paperSunk = Color(0xFFEFEBDF),
    ink = Color(0xFF191712),
    inkMuted = Color(0xFF6E6859),
    rule = Color(0xFFDED8C7),
    highlight = Color(0xFFF2C94C),
    selection = Color(0x24191712),
    error = Color(0xFFB3261E),
    isLight = true,
)

val NightVersoColors = VersoColors(
    paper = Color(0xFF16140F),
    paperSunk = Color(0xFF1E1B15),
    ink = Color(0xFFEDE8D8),
    inkMuted = Color(0xFFA19A88),
    rule = Color(0xFF2E2A22),
    highlight = Color(0xFFF2C94C),
    selection = Color(0x33EDE8D8),
    error = Color(0xFFF2B8B5),
    isLight = false,
)

val LocalVersoColors = staticCompositionLocalOf { LightVersoColors }
