package com.axondragonscale.logviewer.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.logviewer.source.LogFacet
import com.axondragonscale.logviewer.ui.filter.FilterSectionHeader
import com.axondragonscale.logviewer.ui.filter.LogTextField
import com.axondragonscale.logviewer.ui.filter.noun
import com.axondragonscale.logviewer.ui.filter.placeholder
import com.axondragonscale.logviewer.ui.style.LogStyle
import com.axondragonscale.logviewer.ui.style.hairlineColor

/** Created by Ronak Harkhani on 11/22/26 */

/** Tall enough for five rows, short enough to leave the field and footer on screen. */
private val RuleListMaxHeight = 240.dp

/** The rule the filter sheet is about to add: which facet, and on which side of the filter. */
internal data class RuleDraft(val facet: LogFacet, val include: Boolean) {

    val title: String get() = if (include) "Include ${facet.noun}" else "Exclude ${facet.noun}"
}

/**
 * Picks one tag or package. The list scrolls on its own, so a source with thousands of tags reads
 * the same as one with three; typing narrows it, and the footer adds a name the list doesn't have.
 */
@Composable
internal fun RuleSheet(
    draft: RuleDraft,
    suggestions: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val accent = if (draft.include) {
        MaterialTheme.colorScheme.primary
    } else {
        LogStyle.warn
    }
    val term = query.text.trim()
    val matches = remember(suggestions, term) {
        if (term.isEmpty()) suggestions else suggestions.filter { it.contains(term, ignoreCase = true) }
    }

    LogBottomSheet(onDismiss = onDismiss, spacing = 12.dp, scrollable = false) {
        SheetHeader(title = draft.title, onClose = onDismiss)

        RuleField(
            value = query,
            onValueChange = { query = it },
            placeholder = draft.facet.placeholder,
            accent = accent,
            include = draft.include,
        )

        if (suggestions.isEmpty()) {
            Text(
                text = "No ${draft.facet.noun}s left to suggest. Type one below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FilterSectionHeader(
                title = if (term.isEmpty()) "Matches" else "Matching “$term”",
                trailing = {
                    Text(
                        text = matches.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            if (matches.isEmpty()) {
                Text(
                    text = "No match. Add “$term” with the button below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = RuleListMaxHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    itemsIndexed(matches, key = { _, value -> value }) { index, value ->
                        if (index > 0) {
                            HorizontalDivider(color = hairlineColor())
                        }
                        RuleRow(
                            value = value,
                            accent = accent,
                            include = draft.include,
                            onClick = { onAdd(value) },
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onAdd(term) },
            enabled = term.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(text = "Add ${draft.facet.noun}", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun RuleField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    accent: Color,
    include: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (include) Icons.Filled.Add else Icons.Filled.Remove,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = accent,
        )
        LogTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RuleRow(
    value: String,
    accent: Color,
    include: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = LogStyle.mono,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = if (include) Icons.Filled.Add else Icons.Filled.Remove,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = accent,
        )
    }
}
