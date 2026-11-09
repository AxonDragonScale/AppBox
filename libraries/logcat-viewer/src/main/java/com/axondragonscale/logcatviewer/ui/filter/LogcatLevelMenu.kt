package com.axondragonscale.logcatviewer.ui.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.LogLevel
import com.axondragonscale.logcatviewer.ui.chrome.LogcatMenu
import com.axondragonscale.logcatviewer.ui.chrome.LogcatMenuRow
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle
import com.axondragonscale.logcatviewer.ui.style.LogcatDivider
import com.axondragonscale.logcatviewer.ui.style.colors

/** Created by Ronak on 09/11/26 */

@Composable
internal fun LevelMenu(
    label: String,
    levels: Set<LogLevel>,
    onToggle: (LogLevel, Boolean) -> Unit,
    onAllLevels: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(role = Role.DropdownList, onClick = { expanded = true })
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        LogcatMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LogLevel.entries.forEach { level ->
                val selected = level in levels
                LogcatMenuRow(selected = selected, onClick = { onToggle(level, !selected) }) {
                    Text(
                        text = level.letter.toString(),
                        style = LocalLogcatStyle.current.mono.copy(fontWeight = FontWeight.SemiBold),
                        color = level.colors().glyph,
                    )
                    Text(
                        text = level.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            LogcatDivider(Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 8.dp),
                onClick = {
                    expanded = false
                    onAllLevels()
                },
                text = {
                    Text(
                        text = "All levels",
                        style = LocalLogcatStyle.current.action,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}
