package com.axondragonscale.logviewer.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.ui.style.LocalLogStyle
import com.axondragonscale.logviewer.ui.style.colors
import com.axondragonscale.logviewer.ui.style.hairlineColor

/** Created by Ronak Harkhani on 11/22/26 */

/** "All levels" or "4 levels", with every level toggled from one menu. */
@Composable
internal fun LogLevelMenu(
    levels: Set<LogLevel>,
    onToggle: (LogLevel, Boolean) -> Unit,
    onAllLevels: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val style = LocalLogStyle.current
    val label = if (levels.size == LogLevel.entries.size) "All levels" else "${levels.size} levels"

    Box {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(role = Role.Button, onClick = { expanded = true })
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = style.action.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp),
        ) {
            // The panel insets its rows by 8; the menu already pads its own top and bottom.
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                LogLevel.entries.forEachIndexed { index, level ->
                    val enabled = level in levels
                    val above = LogLevel.entries.getOrNull(index - 1)?.let { it in levels } ?: false
                    val below = LogLevel.entries.getOrNull(index + 1)?.let { it in levels } ?: false
                    // Selected neighbours share one block: only its outer corners are rounded.
                    val shape = RoundedCornerShape(
                        topStart = if (enabled && !above) 8.dp else 0.dp,
                        topEnd = if (enabled && !above) 8.dp else 0.dp,
                        bottomStart = if (enabled && !below) 8.dp else 0.dp,
                        bottomEnd = if (enabled && !below) 8.dp else 0.dp,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(shape)
                            .background(if (enabled) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable(role = Role.Checkbox, onClick = { onToggle(level, !enabled) })
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = level.letter.toString(),
                            style = style.mono.copy(fontWeight = FontWeight.SemiBold),
                            color = level.colors().glyph,
                        )
                        Text(
                            text = level.label,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        // The slot is always here, so a check never shifts the names.
                        Box(modifier = Modifier.size(16.dp)) {
                            if (enabled) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = hairlineColor(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clickable(role = Role.Button, onClick = {
                            expanded = false
                            onAllLevels()
                        })
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "All levels",
                        style = style.action.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
