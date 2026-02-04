package com.axondragonscale.wallmatic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.axondragonscale.wallmatic.R

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

// Set of Material typography styles to start with
val Typography = Typography().run {
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
