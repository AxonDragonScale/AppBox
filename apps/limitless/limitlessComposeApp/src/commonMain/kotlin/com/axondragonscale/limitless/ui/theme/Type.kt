package com.axondragonscale.limitless.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.axondragonscale.limitless.resources.Res
import com.axondragonscale.limitless.resources.kodchasan_bold
import com.axondragonscale.limitless.resources.kodchasan_bold_italic
import com.axondragonscale.limitless.resources.kodchasan_extra_light
import com.axondragonscale.limitless.resources.kodchasan_extra_light_italic
import com.axondragonscale.limitless.resources.kodchasan_italic
import com.axondragonscale.limitless.resources.kodchasan_light
import com.axondragonscale.limitless.resources.kodchasan_light_italic
import com.axondragonscale.limitless.resources.kodchasan_medium
import com.axondragonscale.limitless.resources.kodchasan_medium_italic
import com.axondragonscale.limitless.resources.kodchasan_regular
import com.axondragonscale.limitless.resources.kodchasan_semi_bold
import com.axondragonscale.limitless.resources.kodchasan_semi_bold_italic
import org.jetbrains.compose.resources.Font

@Composable
fun kodchasanFontFamily(): FontFamily = FontFamily(
    Font(Res.font.kodchasan_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.kodchasan_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.kodchasan_extra_light, FontWeight.ExtraLight, FontStyle.Normal),
    Font(Res.font.kodchasan_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.kodchasan_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.kodchasan_light, FontWeight.Light, FontStyle.Normal),
    Font(Res.font.kodchasan_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.kodchasan_medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.kodchasan_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.kodchasan_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.kodchasan_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.kodchasan_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
)

@Composable
fun kodchasanTypography(): Typography {
    val fontFamily = kodchasanFontFamily()
    return Typography().run {
        copy(
            displayLarge = displayLarge.copy(fontFamily = fontFamily),
            displayMedium = displayMedium.copy(fontFamily = fontFamily),
            displaySmall = displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = titleLarge.copy(fontFamily = fontFamily),
            titleMedium = titleMedium.copy(fontFamily = fontFamily),
            titleSmall = titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = bodySmall.copy(fontFamily = fontFamily),
            labelLarge = labelLarge.copy(fontFamily = fontFamily),
            labelMedium = labelMedium.copy(fontFamily = fontFamily),
            labelSmall = labelSmall.copy(fontFamily = fontFamily),
        )
    }
}
