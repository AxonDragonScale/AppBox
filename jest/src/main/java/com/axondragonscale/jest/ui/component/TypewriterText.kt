package com.axondragonscale.jest.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * Created by Ronak Harkhani on 06/04/24
 */

enum class TypewriterTextVisibility {
    Gone,
    Invisible,
    Static,
    Animate,
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    visibility: TypewriterTextVisibility = TypewriterTextVisibility.Animate,
    onEffectComplete: (suspend () -> Unit)? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    Box {
        // Reserve space for the full text
        if (visibility != TypewriterTextVisibility.Gone) {
            // Show text if visibility is static, otherwise reserve space
            val alpha = if (visibility == TypewriterTextVisibility.Static) 1f else 0f
            Text(
                modifier = modifier.alpha(alpha),
                text = text,
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout,
                style = style,
            )
        }

        if (visibility == TypewriterTextVisibility.Animate) {
            // Text with typewriter effect
            var displayText by remember(text) { mutableStateOf("") }
            Text(
                modifier = modifier.typewriterEffect(
                    text = text, // Start typewriter effect when showText is true
                    onTextUpdate = { displayText = it },
                    onEffectComplete = onEffectComplete,
                ),
                text = displayText,
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout,
                style = style,
            )
        }
    }
}
