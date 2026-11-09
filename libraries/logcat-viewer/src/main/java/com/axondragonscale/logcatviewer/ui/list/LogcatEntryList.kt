package com.axondragonscale.logcatviewer.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.logcatviewer.model.LogEntry
import com.axondragonscale.logcatviewer.ui.preview.LogcatPreviewTheme
import com.axondragonscale.logcatviewer.ui.preview.PreviewLogEntries
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle
import com.axondragonscale.logcatviewer.ui.style.colors

/** Created by Ronak on 09/11/26 */

@Composable
internal fun LogcatEntryList(
    listState: LazyListState,
    entries: List<LogEntry>,
    wrap: Boolean,
    onSelect: (LogEntry) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        items(items = entries, key = { it.id }, contentType = { it.level }) { entry ->
            LogcatEntryRow(entry = entry, wrap = wrap, onClick = { onSelect(entry) })
        }
    }
}

@Composable
private fun LogcatEntryRow(
    entry: LogEntry,
    wrap: Boolean,
    onClick: () -> Unit,
) {
    val style = LocalLogcatStyle.current
    val colors = entry.level.colors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.row)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.time,
                style = style.mono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = entry.level.letter.toString(),
                style = style.mono.copy(fontWeight = FontWeight.SemiBold),
                color = colors.glyph,
            )
            Text(
                text = entry.tag,
                modifier = Modifier.weight(1f),
                style = style.mono.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = entry.firstLine,
            style = style.mono,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (wrap) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (entry.trace.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(style.traceSurface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                LogcatTrace(lines = entry.trace, style = style.mono, maxLines = 12)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogcatEntryListPreview() {
    LogcatPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LogcatEntryList(
                listState = rememberLazyListState(),
                entries = PreviewLogEntries,
                wrap = false,
                onSelect = {},
            )
        }
    }
}
