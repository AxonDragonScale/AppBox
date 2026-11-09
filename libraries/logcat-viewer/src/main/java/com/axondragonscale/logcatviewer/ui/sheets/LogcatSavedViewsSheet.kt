package com.axondragonscale.logcatviewer.ui.sheets

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.LogFilter
import com.axondragonscale.logcatviewer.model.SavedView
import com.axondragonscale.logcatviewer.ui.chrome.LogcatMenu
import com.axondragonscale.logcatviewer.ui.filter.LogcatRuleChip
import com.axondragonscale.logcatviewer.ui.filter.NoneChip
import com.axondragonscale.logcatviewer.ui.preview.LogcatPreviewTheme
import com.axondragonscale.logcatviewer.ui.preview.PreviewFilter
import com.axondragonscale.logcatviewer.ui.preview.PreviewViews
import com.axondragonscale.logcatviewer.ui.style.LogcatDivider

/** Created by Ronak on 09/11/26 */

@Composable
internal fun SavedViewsSheet(
    filter: LogFilter,
    views: List<SavedView>,
    onApply: (SavedView) -> Unit,
    onSaveCurrent: () -> Unit,
    onRename: (SavedView) -> Unit,
    onDelete: (SavedView) -> Unit,
    onClose: () -> Unit,
) {
    LogcatBottomSheet(onDismiss = onClose) {
        SavedViewsContent(
            filter = filter,
            views = views,
            onApply = onApply,
            onSaveCurrent = onSaveCurrent,
            onRename = onRename,
            onDelete = onDelete,
            onClose = onClose,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SavedViewsContent(
    filter: LogFilter,
    views: List<SavedView>,
    onApply: (SavedView) -> Unit,
    onSaveCurrent: () -> Unit,
    onRename: (SavedView) -> Unit,
    onDelete: (SavedView) -> Unit,
    onClose: () -> Unit,
) {
    var menuFor by remember { mutableStateOf<Long?>(null) }

    SheetHeader(title = "Saved views", onClose = onClose)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetLabel("CURRENT FILTERS")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (filter.rules.isEmpty()) {
                        NoneChip()
                    } else {
                        filter.rules.forEach { rule ->
                            LogcatRuleChip(rule, container = MaterialTheme.colorScheme.surface)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = filter.summary(includeRules = false),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetLabel("SAVED")
        if (views.isEmpty()) {
            Text(
                text = "No saved views yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column {
                    views.forEachIndexed { index, view ->
                        if (index > 0) {
                            LogcatDivider(Modifier.padding(horizontal = 12.dp))
                        }
                        SavedViewRow(
                            view = view,
                            menuOpen = menuFor == view.id,
                            onApply = { onApply(view) },
                            onMenu = { menuFor = view.id },
                            onDismissMenu = { menuFor = null },
                            onRename = {
                                menuFor = null
                                onRename(view)
                            },
                            onDelete = {
                                menuFor = null
                                onDelete(view)
                            },
                        )
                    }
                }
            }
        }
    }

    Text(
        text = "A view stores levels, tags, packages and the search text. Tap one to restore it.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    SheetPrimaryButton(text = "Save current filters", onClick = onSaveCurrent)
}

@Composable
private fun SavedViewRow(
    view: SavedView,
    menuOpen: Boolean,
    onApply: () -> Unit,
    onMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(role = Role.Button, onClick = onApply)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = view.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = view.filter.summary(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Box {
            IconButton(onClick = onMenu, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "View actions",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LogcatMenu(expanded = menuOpen, onDismissRequest = onDismissMenu) {
                DropdownMenuItem(
                    onClick = onRename,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    text = { Text("Rename", style = MaterialTheme.typography.bodyMedium) },
                )
                DropdownMenuItem(
                    onClick = onDelete,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    text = {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SavedViewsPreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SheetColumn {
                SavedViewsContent(
                    filter = PreviewFilter,
                    views = PreviewViews,
                    onApply = {},
                    onSaveCurrent = {},
                    onRename = {},
                    onDelete = {},
                    onClose = {},
                )
            }
        }
    }
}
