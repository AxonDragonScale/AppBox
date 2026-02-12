package com.axondragonscale.npinner.ui.notificationEditor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.npinner.R
import com.axondragonscale.npinner.ui.common.IconLabel
import com.axondragonscale.npinner.ui.common.NPinnerTextField
import com.axondragonscale.npinner.ui.theme.NPinnerTheme

/**
 * Created by Ronak Harkhani on 29/04/23
 */

@Composable
fun Editor(
    title: String,
    description: String,
    onContentChange: (title: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        IconLabel(
            iconPainter = painterResource(id = R.drawable.ic_npinner_icon),
            labelText = "NPINNER",
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { if (title.isBlank()) focusRequester.requestFocus() }
        NPinnerTextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = title,
            onValueChange = { onContentChange(it, description) },
            placeholder = "Title",
            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            maxChars = 72,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        NPinnerTextField(
            value = description,
            onValueChange = { onContentChange(title, it) },
            placeholder = "Description",
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            maxChars = 400,
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditorPreview() {
    NPinnerTheme {
        Surface {
            Editor(
                title = "Title",
                description = "Description",
                onContentChange = { _, _ -> }
            )
        }
    }
}
