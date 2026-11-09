package com.axondragonscale.logcatviewer.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle

/** Created by Ronak on 09/11/26 */

/**
 * The search field keeps its own [TextFieldValue] while the filter catches up. A `String` value
 * round-tripped through the ViewModel would reset the caret on every keystroke, which reads as
 * letters appearing in reverse order.
 */
@Composable
internal fun LogcatSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    regex: Boolean,
    onRegexChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var lastSent by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != lastSent) {
            field = TextFieldValue(value, TextRange(value.length))
            lastSent = value
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BasicTextField(
            value = field,
            onValueChange = { next ->
                field = next
                lastSent = next.text
                onValueChange(next.text)
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                if (field.text.isEmpty()) {
                    Text(
                        text = "Filter message text",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                inner()
            },
        )
        Box(
            modifier = Modifier
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (regex) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outline,
                )
                .clickable(role = Role.Switch, onClick = { onRegexChange(!regex) })
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = ".*",
                style = LocalLogcatStyle.current.mono,
                color = if (regex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
