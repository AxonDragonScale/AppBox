package com.axondragonscale.logcatviewer.ui.chrome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.LogBuffer
import com.axondragonscale.logcatviewer.ui.LogcatTopBarState
import com.axondragonscale.logcatviewer.ui.LogcatUiEvent
import com.axondragonscale.logcatviewer.ui.style.LogcatDivider
import com.axondragonscale.logcatviewer.ui.style.hairlineColor

/** Created by Ronak on 09/11/26 */

/** The one menu surface the screen uses; the saved-view kebab shares it too. */
@Composable
internal fun LogcatMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = hairlineColor(),
        ),
        content = content,
    )
}

/** A selectable row: the selected one is tinted and checked, so every menu marks choice the same. */
@Composable
internal fun LogcatMenuRow(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
internal fun LogcatOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    state: LogcatTopBarState,
    onEvent: (LogcatUiEvent) -> Unit,
    onOpenBufferMenu: () -> Unit,
    onOpenSavedViews: () -> Unit,
) {
    LogcatMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            contentPadding = PaddingValues(horizontal = 12.dp),
            onClick = onOpenBufferMenu,
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Buffer", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = state.buffer.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        DropdownMenuItem(
            contentPadding = PaddingValues(horizontal = 12.dp),
            onClick = {
                onEvent(LogcatUiEvent.ToggleAutoscroll)
                onDismiss()
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Autoscroll", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    if (state.autoscroll) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
        )
        DropdownMenuItem(
            contentPadding = PaddingValues(horizontal = 12.dp),
            onClick = {
                onEvent(LogcatUiEvent.ToggleWrap)
                onDismiss()
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Wrap long lines", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (state.wrap) "On" else "Off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        DropdownMenuItem(
            contentPadding = PaddingValues(horizontal = 12.dp),
            onClick = onOpenSavedViews,
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Saved views", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        LogcatDivider(Modifier.padding(horizontal = 12.dp))
        DropdownMenuItem(
            contentPadding = PaddingValues(horizontal = 12.dp),
            onClick = {
                onEvent(LogcatUiEvent.Export)
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            text = {
                Text("Export visible", style = MaterialTheme.typography.bodyMedium)
            },
        )
    }
}

@Composable
internal fun BufferMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    selected: LogBuffer,
    onSelect: (LogBuffer) -> Unit,
) {
    LogcatMenu(expanded = expanded, onDismissRequest = onDismiss) {
        LogBuffer.entries.forEach { buffer ->
            val isSelected = buffer == selected
            LogcatMenuRow(selected = isSelected, onClick = { onSelect(buffer) }) {
                Text(
                    text = buffer.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}
