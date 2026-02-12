package com.axondragonscale.npinner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

val DefaultTypography = Typography()

val NPinnerTypography = DefaultTypography.copy(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = FontFamily.Monospace),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = FontFamily.Monospace),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = FontFamily.Monospace),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = FontFamily.Monospace),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = FontFamily.Monospace),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = FontFamily.Monospace),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = FontFamily.Monospace),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = FontFamily.Monospace),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = FontFamily.Monospace),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = FontFamily.Monospace),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = FontFamily.Monospace),
)
