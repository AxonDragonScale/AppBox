package com.axondragonscale.logviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Created by Ronak Harkhani on 11/22/26 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogTopBar(
    title: String,
    caption: String,
    paused: Boolean,
    pendingCount: Int,
    wrap: Boolean,
    canShare: Boolean,
    canClear: Boolean,
    onBack: (() -> Unit)?,
    onTogglePause: () -> Unit,
    onJumpToNewest: () -> Unit,
    onToggleWrap: (Boolean) -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit,
) {
    var overflow by remember { mutableStateOf(false) }

    TopAppBar(
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        title = {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (paused) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                shape = CircleShape,
                            ),
                    )
                    Text(
                        text = if (paused) {
                            if (pendingCount > 0) "Paused · $pendingCount new" else "Paused"
                        } else {
                            if (caption.isBlank()) "Live" else "Live · $caption"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onTogglePause) {
                Icon(
                    imageVector = if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (paused) "Resume" else "Pause",
                    tint = if (paused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onJumpToNewest) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = "Jump to newest",
                    tint = if (pendingCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            Box {
                IconButton(onClick = { overflow = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
                    DropdownMenuItem(
                        text = { Text("Wrap long lines") },
                        trailingIcon = {
                            Switch(checked = wrap, onCheckedChange = { onToggleWrap(it) })
                        },
                        onClick = { onToggleWrap(!wrap) },
                    )
                    if (canShare) {
                        DropdownMenuItem(
                            text = { Text("Export visible") },
                            leadingIcon = {
                                Icon(Icons.Filled.Share, contentDescription = null)
                            },
                            onClick = {
                                overflow = false
                                onShare()
                            },
                        )
                    }
                    if (canClear) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text("Clear logs", color = MaterialTheme.colorScheme.error)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                overflow = false
                                onClear()
                            },
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
