package com.axondragonscale.jest.ui.theme

import androidx.compose.ui.graphics.Color

// region Dark Theme

val DarkBackground = Color(0xFF0B0B0E)
val DarkSurface = Color(0xFF16161A)
val DarkSurfaceVariant = Color(0xFF1A1A1E)
val DarkBorder = Color(0xFF2A2A2E)

val DarkPrimary = Color(0xFF6366F1)       // Indigo — active nav, badges, CTA
val DarkPrimaryContainer = Color(0x206366F1)
val DarkSecondary = Color(0xFFFFB547)     // Amber — settings highlight, day streak
val DarkSecondaryContainer = Color(0x20FFB547)
val DarkError = Color(0xFFE85A4F)         // Favorite hearts, dark category badge
val DarkErrorContainer = Color(0x20E85A4F)
val DarkSuccess = Color(0xFF32D583)       // Pun badge, jokes seen stat
val DarkSuccessContainer = Color(0x2032D583)

val DarkOnBackground = Color(0xFFFAFAF9)  // Text primary
val DarkOnSurface = Color(0xFFFAFAF9)
val DarkOnPrimary = Color(0xFFFAFAF9)
val DarkTextSecondary = Color(0xFF4A4A50) // Inactive nav, placeholder
val DarkTextTertiary = Color(0xFF6B6B70)  // Section labels

// endregion

// region Light Theme

val LightBackground = Color(0xFFF5F5F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F0F5)
val LightBorder = Color(0xFFE0E0E5)

val LightPrimary = Color(0xFF4F52D9)
val LightPrimaryContainer = Color(0x204F52D9)
val LightSecondary = Color(0xFFCC8C2A)
val LightSecondaryContainer = Color(0x20CC8C2A)
val LightError = Color(0xFFD44940)
val LightErrorContainer = Color(0x20D44940)
val LightSuccess = Color(0xFF22A86A)
val LightSuccessContainer = Color(0x2022A86A)

val LightOnBackground = Color(0xFF0B0B0E)
val LightOnSurface = Color(0xFF0B0B0E)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightTextSecondary = Color(0xFF8A8A90)
val LightTextTertiary = Color(0xFFAAAAAA)

// endregion

// region Category Colors

/**
 * Returns the accent color for a given category name.
 * Used for category badges and stat accents.
 */
fun categoryColor(categoryName: String, isDark: Boolean = true): Color = when (categoryName.uppercase()) {
    "PROGRAMMING" -> if (isDark) DarkPrimary else LightPrimary
    "PUN" -> if (isDark) DarkSuccess else LightSuccess
    "DARK" -> if (isDark) DarkError else LightError
    "MISC", "MISCELLANEOUS" -> if (isDark) DarkSecondary else LightSecondary
    "SPOOKY" -> Color(0xFFAA6EE8)
    "CHRISTMAS" -> Color(0xFF4CAF50)
    else -> if (isDark) DarkPrimary else LightPrimary
}

fun categoryContainerColor(categoryName: String, isDark: Boolean = true): Color =
    categoryColor(categoryName, isDark).copy(alpha = 0.12f)

// endregion
