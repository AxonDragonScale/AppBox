package com.axondragonscale.logcatviewer.ui.filter

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
import com.axondragonscale.logcatviewer.model.FilterKind
import com.axondragonscale.logcatviewer.model.LogFilter
import com.axondragonscale.logcatviewer.ui.LogcatUiEvent

/** Created by Ronak on 09/11/26 */

/** A rule the user asked to add, before the sheet's dialog supplies its value. */
internal data class FilterDraft(val kind: FilterKind, val include: Boolean)

@Composable
internal fun LogcatFilterBar(
    filter: LogFilter,
    onEvent: (LogcatUiEvent) -> Unit,
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
        LogcatSearchField(
            value = filter.query,
            onValueChange = { onEvent(LogcatUiEvent.SetQuery(it)) },
            regex = filter.regex,
            onRegexChange = { onEvent(LogcatUiEvent.SetRegex(it)) },
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
        LevelMenu(
            label = filter.levelLabel,
            levels = filter.levels,
            onToggle = { level, enabled -> onEvent(LogcatUiEvent.SetLevel(level, enabled)) },
            onAllLevels = { onEvent(LogcatUiEvent.SelectAllLevels) },
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filter.rules.forEach { rule ->
                LogcatRuleChip(rule, onRemove = { onEvent(LogcatUiEvent.RemoveRule(rule)) })
            }
        }
    }
}
