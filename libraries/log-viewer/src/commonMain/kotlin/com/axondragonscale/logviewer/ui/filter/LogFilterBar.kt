package com.axondragonscale.logviewer.ui.filter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.logviewer.source.LogQuery
import com.axondragonscale.logviewer.ui.LogUiEvent

/** Created by Ronak Harkhani on 11/22/26 */

@Composable
internal fun LogFilterBar(
    query: LogQuery,
    onEvent: (LogUiEvent) -> Unit,
    onOpenFilters: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogSearchField(
            value = query.text,
            onValueChange = { onEvent(LogUiEvent.SetText(it)) },
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
        )
        IconButton(onClick = onOpenFilters, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = "Filters",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogLevelMenu(
            levels = query.levels,
            onToggle = { level, enabled -> onEvent(LogUiEvent.SetLevel(level, enabled)) },
            onAllLevels = { onEvent(LogUiEvent.SelectAllLevels) },
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            query.rules.forEach { rule ->
                RuleChip(
                    rule = rule,
                    onRemove = { onEvent(LogUiEvent.RemoveRule(rule.facet, rule.value)) },
                )
            }
        }
    }
}
