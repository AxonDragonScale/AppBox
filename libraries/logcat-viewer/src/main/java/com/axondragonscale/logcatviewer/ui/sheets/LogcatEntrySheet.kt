package com.axondragonscale.logcatviewer.ui.sheets

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.LogLevel
import com.axondragonscale.logcatviewer.ui.LogcatSelection
import com.axondragonscale.logcatviewer.ui.filter.ruleAction
import com.axondragonscale.logcatviewer.ui.list.LogcatTrace
import com.axondragonscale.logcatviewer.ui.preview.LogcatPreviewTheme
import com.axondragonscale.logcatviewer.ui.preview.PreviewSelection
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle
import com.axondragonscale.logcatviewer.ui.style.colors

/** Created by Ronak on 09/11/26 */

@Composable
internal fun EntrySheet(
    selection: LogcatSelection,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onIncludeTag: () -> Unit,
    onExcludeTag: () -> Unit,
) {
    LogcatBottomSheet(onDismiss = onDismiss, spacing = 10.dp) {
        EntrySheetContent(
            selection = selection,
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
internal fun EntrySheetContent(
    selection: LogcatSelection,
    onClose: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onIncludeTag: () -> Unit,
    onExcludeTag: () -> Unit,
) {
    val entry = selection.entry
    val style = LocalLogcatStyle.current
    SheetTitleRow(onClose) {
        LevelBadge(level = entry.level)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = entry.tag,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = entry.time,
                style = style.mono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = entry.firstLine,
                style = style.mono,
                color = MaterialTheme.colorScheme.onSurface,
            )
            LogcatTrace(lines = entry.trace, style = style.mono)
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MetaChip("PID ${entry.pid}")
        MetaChip("TID ${entry.tid}")
        MetaChip(selection.packageName ?: "package unknown")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SheetAction(
            icon = Icons.Filled.ContentCopy,
            label = "Copy",
            color = MaterialTheme.colorScheme.primary,
            onClick = onCopy,
        )
        val include = ruleAction(include = true)
        SheetAction(
            icon = include.icon,
            label = include.label,
            color = include.tint,
            onClick = onIncludeTag,
        )
        val exclude = ruleAction(include = false)
        SheetAction(
            icon = exclude.icon,
            label = exclude.label,
            color = exclude.tint,
            onClick = onExcludeTag,
        )
        SheetAction(
            icon = Icons.Filled.Share,
            label = "Share",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onShare,
        )
    }

    Text(
        text = "Include adds this tag to the filters. Exclude drops every line with it.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LevelBadge(level: LogLevel) {
    val colors = level.colors()
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.badge),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = level.letter.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.glyph,
        )
    }
}

@Composable
private fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = LocalLogcatStyle.current.mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    color: Color,
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
            tint = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EntrySheetPreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SheetColumn(spacing = 10.dp) {
                EntrySheetContent(
                    selection = PreviewSelection,
                    onClose = {},
                    onCopy = {},
                    onShare = {},
                    onIncludeTag = {},
                    onExcludeTag = {},
                )
            }
        }
    }
}
