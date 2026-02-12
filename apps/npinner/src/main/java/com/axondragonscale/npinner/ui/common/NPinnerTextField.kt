package com.axondragonscale.npinner.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.npinner.ui.theme.NPinnerTheme

/**
 * Created by Ronak Harkhani on 28/04/23
 */

@Composable
fun NPinnerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    maxLines: Int = Int.MAX_VALUE,
    maxChars: Int? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    // If color is not provided via the text style, use onSurface color as a default
    val textColor = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onBackground }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    BasicTextField(
        value = value,
        modifier = modifier.defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
            minHeight = TextFieldDefaults.MinHeight
        ),
        onValueChange = { newValue ->
            if (maxChars != null && newValue.length <= maxChars) onValueChange(newValue)
        },
        textStyle = mergedTextStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        singleLine = maxLines == 1,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            UnderlinedDecorationBox(
                value = value,
                innerTextField = innerTextField,
                placeholder = placeholder,
                textStyle = mergedTextStyle,
                interactionSource = interactionSource,
                maxChars = maxChars,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnderlinedDecorationBox(
    value: String,
    innerTextField: @Composable () -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    maxChars: Int?,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Box {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = textStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5F)
                    ),
                )
            }
            innerTextField()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .indicatorLine(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = TextFieldDefaults.colors()
                )
        )

        maxChars?.let {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    text = "${value.length}/$maxChars",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NPinnerTextFieldPreview() {
    NPinnerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            var text by remember { mutableStateOf("") }
            NPinnerTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Placeholder",
                maxChars = 20,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
