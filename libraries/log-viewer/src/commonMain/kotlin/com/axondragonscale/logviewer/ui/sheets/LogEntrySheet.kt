package com.axondragonscale.logviewer.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.ui.logFullTime
import com.axondragonscale.logviewer.ui.style.LocalLogStyle

/** Created by Ronak Harkhani on 11/22/26 */

@Composable
internal fun LogEntrySheet(
    record: LogRecord,
    capabilities: LogCapabilities,
    onDismiss: () -> Unit,
    onCopy: ((String) -> Unit)?,
    onShare: ((String) -> Unit)?,
    onIncludeTag: (String) -> Unit,
    onExcludeTag: (String) -> Unit,
) {
    LogBottomSheet(onDismiss = onDismiss, spacing = 10.dp) {
        LogEntrySheetContent(
            record = record,
            capabilities = capabilities,
            onClose = onDismiss,
            onCopy = onCopy,
            onShare = onShare,
            onIncludeTag = onIncludeTag,
            onExcludeTag = onExcludeTag,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogEntrySheetContent(
    record: LogRecord,
    capabilities: LogCapabilities,
    onClose: () -> Unit,
    onCopy: ((String) -> Unit)?,
    onShare: ((String) -> Unit)?,
    onIncludeTag: (String) -> Unit,
    onExcludeTag: (String) -> Unit,
) {
    val style = LocalLogStyle.current
    val trace = record.message.lineSequence().drop(1).toList()
    val messageStyle = style.mono.copy(lineHeight = 17.sp)

    SheetTitleRow(onClose) {
        LevelBadge(level = record.level)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = record.tag,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = record.time.logFullTime(),
                style = style.mono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = record.firstLine,
                style = messageStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            trace.forEachIndexed { index, line ->
                Text(
                    text = line,
                    style = messageStyle,
                    color = if (index == 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }

    MetaPills(record = record, capabilities = capabilities)

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (onCopy != null) {
            SheetAction(
                label = "Copy",
                icon = Icons.Filled.ContentCopy,
                tint = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                onClick = { onCopy(record.message) },
            )
        }
        SheetAction(
            label = "Include",
            icon = Icons.Filled.FilterAlt,
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = {
                onIncludeTag(record.tag)
                onClose()
            },
        )
        SheetAction(
            label = "Exclude",
            icon = Icons.Filled.FilterAltOff,
            tint = style.warn,
            onClick = {
                onExcludeTag(record.tag)
                onClose()
            },
        )
        if (onShare != null) {
            SheetAction(
                label = "Share",
                icon = Icons.Filled.Share,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = { onShare(record.message) },
            )
        }
    }

    Text(
        text = "Include adds this tag to the filters. Exclude drops every line with it.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetaPills(record: LogRecord, capabilities: LogCapabilities) {
    val pills = buildList {
        if (capabilities.pid) record.meta.pid?.let { add("PID $it") }
        if (capabilities.tid) record.meta.tid?.let { add("TID $it") }
        if (capabilities.packageName) record.meta.packageName?.let { add(it) }
        if (capabilities.sessions) record.meta.session?.let { add("session ${it.take(6)}") }
    }
    if (pills.isEmpty()) return

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        pills.forEach { pill ->
            Row(
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pill,
                    style = LocalLogStyle.current.mono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SheetAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    fontWeight: FontWeight = FontWeight.Medium,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = tint,
        )
        Text(
            text = label,
            style = LocalLogStyle.current.action.copy(fontSize = 12.sp, fontWeight = fontWeight),
            color = tint,
        )
    }
}
