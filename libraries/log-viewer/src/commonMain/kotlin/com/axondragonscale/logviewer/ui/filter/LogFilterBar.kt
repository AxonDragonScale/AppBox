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
import com.axondragonscale.logviewer.model.LogLevel

/** Created by Ronak Harkhani on 11/22/26 */

@Composable
internal fun LogFilterBar(
    query: String,
    levels: Set<LogLevel>,
    tags: Set<String>,
    excludedTags: Set<String>,
    packages: Set<String>,
    excludedPackages: Set<String>,
    onQueryChange: (String) -> Unit,
    onToggleLevel: (LogLevel, Boolean) -> Unit,
    onAllLevels: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onRemovePackage: (String) -> Unit,
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
            value = query,
            onValueChange = onQueryChange,
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
            levels = levels,
            onToggle = onToggleLevel,
            onAllLevels = onAllLevels,
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tags.forEach { tag ->
                LogTagChip(label = tag, exclude = false, onRemove = { onRemoveTag(tag) })
            }
            excludedTags.forEach { tag ->
                LogTagChip(label = tag, exclude = true, onRemove = { onRemoveTag(tag) })
            }
            packages.forEach { name ->
                LogTagChip(label = "pkg:$name", exclude = false, onRemove = { onRemovePackage(name) })
            }
            excludedPackages.forEach { name ->
                LogTagChip(label = "pkg:$name", exclude = true, onRemove = { onRemovePackage(name) })
            }
        }
    }
}
