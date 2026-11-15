package com.axondragonscale.verso.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Fraunces for the identity, Inter for controls and Literata for the book. The faces are not bundled
 * yet — the system serif and sans stand in until the fonts land, without changing any size or metric.
 */
val DisplayFontFamily = FontFamily.Serif
val UIFontFamily = FontFamily.Default
val ReadingFontFamily = FontFamily.Serif

object VersoTextStyles {

    val display = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    )

    val headline = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    )

    val title = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    )

    val ui = TextStyle(
        fontFamily = UIFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    val uiStrong = TextStyle(
        fontFamily = UIFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    val label = TextStyle(
        fontFamily = UIFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.6.sp,
    )

    val micro = TextStyle(
        fontFamily = UIFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.2.sp,
    )

    val reading = TextStyle(
        fontFamily = ReadingFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    )
}

val VersoTypography = Typography(
    displayLarge = VersoTextStyles.display,
    displayMedium = VersoTextStyles.display,
    headlineMedium = VersoTextStyles.headline,
    headlineSmall = VersoTextStyles.title,
    titleLarge = VersoTextStyles.title,
    titleMedium = VersoTextStyles.uiStrong,
    bodyLarge = VersoTextStyles.ui,
    bodyMedium = VersoTextStyles.ui,
    bodySmall = VersoTextStyles.micro,
    labelLarge = VersoTextStyles.label,
    labelMedium = VersoTextStyles.label,
    labelSmall = VersoTextStyles.micro,
)
