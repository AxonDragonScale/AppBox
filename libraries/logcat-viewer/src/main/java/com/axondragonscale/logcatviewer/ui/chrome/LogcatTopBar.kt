package com.axondragonscale.logcatviewer.ui.chrome

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.ui.LogcatTopBarState
import com.axondragonscale.logcatviewer.ui.LogcatUiEvent

/** Created by Ronak on 09/11/26 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogcatTopBar(
    state: LogcatTopBarState,
    title: String,
    onBack: (() -> Unit)?,
    onEvent: (LogcatUiEvent) -> Unit,
    onJump: () -> Unit,
    onOpenSavedViews: () -> Unit,
) {
    var overflow by remember { mutableStateOf(false) }
    var bufferMenu by remember { mutableStateOf(false) }

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
                                color = if (state.paused) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                shape = CircleShape,
                            ),
                    )
                    Text(
                        text = if (state.paused) {
                            "Paused · ${state.buffer.caption}"
                        } else {
                            "Live · ${state.buffer.caption}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { onEvent(LogcatUiEvent.TogglePause) }) {
                Icon(
                    imageVector = if (state.paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (state.paused) "Resume" else "Pause",
                    tint = if (state.paused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            IconButton(onClick = onJump) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = "Jump to bottom",
                    tint = if (state.pendingCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            IconButton(onClick = { onEvent(LogcatUiEvent.ClearLog) }) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Clear log",
                    tint = MaterialTheme.colorScheme.onSurface,
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
                LogcatOverflowMenu(
                    expanded = overflow,
                    onDismiss = { overflow = false },
                    state = state,
                    onEvent = onEvent,
                    onOpenBufferMenu = {
                        overflow = false
                        bufferMenu = true
                    },
                    onOpenSavedViews = {
                        overflow = false
                        onOpenSavedViews()
                    },
                )
                BufferMenu(
                    expanded = bufferMenu,
                    onDismiss = { bufferMenu = false },
                    selected = state.buffer,
                    onSelect = {
                        bufferMenu = false
                        onEvent(LogcatUiEvent.SelectBuffer(it))
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}
