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
import com.axondragonscale.logviewer.source.LogFacet
import com.axondragonscale.logviewer.source.LogFacets
import com.axondragonscale.logviewer.source.LogRule
import com.axondragonscale.logviewer.ui.LogUiEvent
import com.axondragonscale.logviewer.ui.LogUiState
import com.axondragonscale.logviewer.ui.filter.FilterSectionHeader
import com.axondragonscale.logviewer.ui.filter.IncludeExcludeButtons
import com.axondragonscale.logviewer.ui.filter.RuleChip
import com.axondragonscale.logviewer.ui.filter.emptyHint
import com.axondragonscale.logviewer.ui.filter.isSupportedBy
import com.axondragonscale.logviewer.ui.filter.sectionTitle

/** Created by Ronak Harkhani on 11/22/26 */

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LogFilterSheet(
    state: LogUiState,
    facets: LogFacets,
    lineCount: Int,
    onEvent: (LogUiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf<RuleDraft?>(null) }

    // One sheet at a time: the picker replaces the filter sheet, and dismissing it returns there.
    val current = draft
    if (current == null) {
        LogBottomSheet(onDismiss = onDismiss) {
            SheetHeader(title = "Filters", onClose = onDismiss)

            LogFacet.entries.forEach { facet ->
                if (facet.isSupportedBy(state.capabilities)) {
                    FilterSectionHeader(
                        title = facet.sectionTitle,
                        trailing = {
                            IncludeExcludeButtons(
                                onInclude = { draft = RuleDraft(facet, include = true) },
                                onExclude = { draft = RuleDraft(facet, include = false) },
                            )
                        },
                    )
                    val rules = state.query.rules.filter { it.facet == facet }
                    if (rules.isEmpty()) {
                        Text(
                            text = facet.emptyHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            rules.forEach { rule ->
                                RuleChip(
                                    rule = rule,
                                    onRemove = { onEvent(LogUiEvent.RemoveRule(facet, rule.value)) },
                                )
                            }
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
        val applied = state.query.included(current.facet) + state.query.excluded(current.facet)
        RuleSheet(
            draft = current,
            suggestions = facets.values(current.facet) - applied,
            onDismiss = { draft = null },
            onAdd = { value ->
                onEvent(LogUiEvent.AddRule(current.facet, value, current.include))
                draft = null
            },
        )
    }
}
