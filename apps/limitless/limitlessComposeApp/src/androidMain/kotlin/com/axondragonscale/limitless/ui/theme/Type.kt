package com.axondragonscale.limitless.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.axondragonscale.limitless.R

val KodchasanFontFamily = FontFamily(
    Font(R.font.kodchasan_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.kodchasan_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.kodchasan_extra_light, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.kodchasan_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.kodchasan_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.kodchasan_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.kodchasan_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.kodchasan_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.kodchasan_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.kodchasan_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.kodchasan_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.kodchasan_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
)

val KodchasanTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = KodchasanFontFamily),
        displayMedium = displayMedium.copy(fontFamily = KodchasanFontFamily),
        displaySmall = displaySmall.copy(fontFamily = KodchasanFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = KodchasanFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = KodchasanFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = KodchasanFontFamily),
        titleLarge = titleLarge.copy(fontFamily = KodchasanFontFamily),
        titleMedium = titleMedium.copy(fontFamily = KodchasanFontFamily),
        titleSmall = titleSmall.copy(fontFamily = KodchasanFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = KodchasanFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = KodchasanFontFamily),
        bodySmall = bodySmall.copy(fontFamily = KodchasanFontFamily),
        labelLarge = labelLarge.copy(fontFamily = KodchasanFontFamily),
        labelMedium = labelMedium.copy(fontFamily = KodchasanFontFamily),
        labelSmall = labelSmall.copy(fontFamily = KodchasanFontFamily),
    )
}
