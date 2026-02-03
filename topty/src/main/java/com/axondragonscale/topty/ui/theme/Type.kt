package com.axondragonscale.topty.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.axondragonscale.topty.R

val GeoFontFamily = FontFamily(
    Font(R.font.geo_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.geo_italic, FontWeight.Normal, FontStyle.Italic),
)

val GeoTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = GeoFontFamily),
        displayMedium = displayMedium.copy(fontFamily = GeoFontFamily),
        displaySmall = displaySmall.copy(fontFamily = GeoFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = GeoFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = GeoFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = GeoFontFamily),
        titleLarge = titleLarge.copy(fontFamily = GeoFontFamily),
        titleMedium = titleMedium.copy(fontFamily = GeoFontFamily),
        titleSmall = titleSmall.copy(fontFamily = GeoFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = GeoFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = GeoFontFamily),
        bodySmall = bodySmall.copy(fontFamily = GeoFontFamily),
        labelLarge = labelLarge.copy(fontFamily = GeoFontFamily),
        labelMedium = labelMedium.copy(fontFamily = GeoFontFamily),
        labelSmall = labelSmall.copy(fontFamily = GeoFontFamily),
    )
}

val ChakraPetchFontFamily = FontFamily(
    Font(R.font.chakra_petch_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.chakra_petch_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.chakra_petch_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.chakra_petch_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.chakra_petch_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.chakra_petch_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.chakra_petch_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.chakra_petch_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.chakra_petch_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
)

val ChakraPetchTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = ChakraPetchFontFamily),
        displayMedium = displayMedium.copy(fontFamily = ChakraPetchFontFamily),
        displaySmall = displaySmall.copy(fontFamily = ChakraPetchFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = ChakraPetchFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = ChakraPetchFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = ChakraPetchFontFamily),
        titleLarge = titleLarge.copy(fontFamily = ChakraPetchFontFamily),
        titleMedium = titleMedium.copy(fontFamily = ChakraPetchFontFamily),
        titleSmall = titleSmall.copy(fontFamily = ChakraPetchFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = ChakraPetchFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = ChakraPetchFontFamily),
        bodySmall = bodySmall.copy(fontFamily = ChakraPetchFontFamily),
        labelLarge = labelLarge.copy(fontFamily = ChakraPetchFontFamily),
        labelMedium = labelMedium.copy(fontFamily = ChakraPetchFontFamily),
        labelSmall = labelSmall.copy(fontFamily = ChakraPetchFontFamily),
    )
}

val OxaniumFontFamily = FontFamily(
    Font(R.font.oxanium_extra_light, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.oxanium_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.oxanium_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.oxanium_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.oxanium_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.oxanium_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.oxanium_extra_bold, FontWeight.ExtraBold, FontStyle.Normal),
)

val OxaniumTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = OxaniumFontFamily),
        displayMedium = displayMedium.copy(fontFamily = OxaniumFontFamily),
        displaySmall = displaySmall.copy(fontFamily = OxaniumFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = OxaniumFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = OxaniumFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = OxaniumFontFamily),
        titleLarge = titleLarge.copy(fontFamily = OxaniumFontFamily),
        titleMedium = titleMedium.copy(fontFamily = OxaniumFontFamily),
        titleSmall = titleSmall.copy(fontFamily = OxaniumFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = OxaniumFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = OxaniumFontFamily),
        bodySmall = bodySmall.copy(fontFamily = OxaniumFontFamily),
        labelLarge = labelLarge.copy(fontFamily = OxaniumFontFamily),
        labelMedium = labelMedium.copy(fontFamily = OxaniumFontFamily),
        labelSmall = labelSmall.copy(fontFamily = OxaniumFontFamily),
    )
}
