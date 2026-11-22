package com.axondragonscale.logviewer.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.logviewer.ui.LogUiState
import com.axondragonscale.logviewer.ui.filter.FilterSectionHeader
import com.axondragonscale.logviewer.ui.filter.IncludeExcludeButtons
import com.axondragonscale.logviewer.ui.filter.LogTagChip

/** Created by Ronak Harkhani on 11/22/26 */

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LogFilterSheet(
    state: LogUiState,
    availableTags: List<String>,
    availablePackages: List<String>,
    lineCount: Int,
    onIncludeTag: (String) -> Unit,
    onExcludeTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onIncludePackage: (String) -> Unit,
    onExcludePackage: (String) -> Unit,
    onRemovePackage: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf<RuleDraft?>(null) }
    val tagSuggestions = remember(availableTags, state.tags, state.excludedTags) {
        availableTags - state.tags - state.excludedTags
    }
    val packageSuggestions = remember(availablePackages, state.packages, state.excludedPackages) {
        availablePackages - state.packages - state.excludedPackages
    }

    // One sheet at a time: the picker replaces the filter sheet, and dismissing it returns there.
    val current = draft
    if (current == null) {
        LogBottomSheet(onDismiss = onDismiss) {
            SheetHeader(title = "Filters", onClose = onDismiss)

            FilterSectionHeader(
                title = "Tag",
                trailing = {
                    IncludeExcludeButtons(
                        onInclude = { draft = RuleDraft(RuleKind.TAG, include = true) },
                        onExclude = { draft = RuleDraft(RuleKind.TAG, include = false) },
                    )
                },
            )
            if (state.tags.isEmpty() && state.excludedTags.isEmpty()) {
                Text(
                    text = "No tag rules. Include keeps a tag's lines; exclude drops them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    state.tags.forEach { tag ->
                        LogTagChip(label = tag, exclude = false, onRemove = { onRemoveTag(tag) })
                    }
                    state.excludedTags.forEach { tag ->
                        LogTagChip(label = tag, exclude = true, onRemove = { onRemoveTag(tag) })
                    }
                }
            }

            if (state.capabilities.packageName) {
                FilterSectionHeader(
                    title = "Package",
                    trailing = {
                        IncludeExcludeButtons(
                            onInclude = { draft = RuleDraft(RuleKind.PACKAGE, include = true) },
                            onExclude = { draft = RuleDraft(RuleKind.PACKAGE, include = false) },
                        )
                    },
                )
                if (state.packages.isEmpty() && state.excludedPackages.isEmpty()) {
                    Text(
                        text = "No package rules. A package follows the app's live PIDs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.packages.forEach { name ->
                            LogTagChip(label = "pkg:$name", exclude = false, onRemove = { onRemovePackage(name) })
                        }
                        state.excludedPackages.forEach { name ->
                            LogTagChip(label = "pkg:$name", exclude = true, onRemove = { onRemovePackage(name) })
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("Show $lineCount lines", style = MaterialTheme.typography.titleSmall)
            }
        }
    } else {
        RuleSheet(
            draft = current,
            suggestions = when (current.kind) {
                RuleKind.TAG -> tagSuggestions
                RuleKind.PACKAGE -> packageSuggestions
            },
            onDismiss = { draft = null },
            onAdd = { value ->
                when {
                    current.kind == RuleKind.TAG && current.include -> onIncludeTag(value)
                    current.kind == RuleKind.TAG -> onExcludeTag(value)
                    current.include -> onIncludePackage(value)
                    else -> onExcludePackage(value)
                }
                draft = null
            },
        )
    }
}
