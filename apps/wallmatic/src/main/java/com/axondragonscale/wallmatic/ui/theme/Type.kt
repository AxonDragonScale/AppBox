package com.axondragonscale.wallmatic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.R

/**
 * Created by Ronak Harkhani on 06/06/24
 */

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

val RobotoFontFamily = FontFamily.Default

object WallmaticTextStyles {

    val display = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    )

    val headline = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    )

    val title = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    )

    val dialogTitle = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    )

    val titleSmall = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

    /** Row and card titles inside the Library/Album/Settings ledgers. */
    val rowTitle = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    )

    val body = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    val caption = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )

    val label = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.6.sp,
    )

    val micro = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.2.sp,
    )

    val microStrong = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.6.sp,
    )

    val button = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.4.sp,
    )

    val time = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )

    val number = TextStyle(
        fontFamily = ChakraPetchFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    )
}

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
        bodyLarge = bodyLarge.copy(fontFamily = RobotoFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = RobotoFontFamily),
        bodySmall = bodySmall.copy(fontFamily = RobotoFontFamily),
        labelLarge = labelLarge.copy(fontFamily = RobotoFontFamily),
        labelMedium = labelMedium.copy(fontFamily = RobotoFontFamily),
        labelSmall = labelSmall.copy(fontFamily = RobotoFontFamily),
    )
}
