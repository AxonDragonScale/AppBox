package com.axondragonscale.storageviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/** Created by Ronak on 14/11/26 */

private val FieldHeight = 40.dp
private val FieldRadius = 16.dp // design: field16
private val FieldIcon = 20.dp
private val FieldIconGap = 8.dp
private val FieldPadding = 10.dp
private val ClearSlot = 40.dp
private val ClearIcon = 18.dp

/**
 * The one search field. [active] adds the clear action; the query itself stays editable in both
 * states so tapping the field never has to re-enter a mode.
 */
@Composable
internal fun StorageSearchField(
    modifier: Modifier = Modifier,
    query: String,
    placeholder: String,
    active: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    var field by remember { mutableStateOf(TextFieldValue(query, TextRange(query.length))) }
    var lastSent by remember { mutableStateOf(query) }

    // A String round-trip through the caller resets the caret on every keystroke.
    LaunchedEffect(query) {
        if (query != lastSent) {
            field = TextFieldValue(query, TextRange(query.length))
            lastSent = query
        }
    }

    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .clip(RoundedCornerShape(FieldRadius))
            .background(scheme.surfaceContainerHigh)
            .border(1.dp, scheme.outline, RoundedCornerShape(FieldRadius))
            .padding(start = FieldPadding, end = if (active) 0.dp else FieldPadding),
        horizontalArrangement = Arrangement.spacedBy(FieldIconGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            modifier = Modifier.size(FieldIcon),
            tint = scheme.onSurfaceVariant,
        )
        BasicTextField(
            value = field,
            onValueChange = { next ->
                field = next
                lastSent = next.text
                onQueryChange(next.text)
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = scheme.onSurface),
            cursorBrush = SolidColor(scheme.primary),
            decorationBox = { inner ->
                if (field.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                inner()
            },
        )
        if (active) {
            Box(
                modifier = Modifier
                    .size(ClearSlot)
                    .clip(RoundedCornerShape(FieldRadius))
                    .clickable(role = Role.Button, onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear search",
                    modifier = Modifier.size(ClearIcon),
                    tint = scheme.onSurfaceVariant,
                )
            }
        }
        trailing?.invoke()
    }
}
