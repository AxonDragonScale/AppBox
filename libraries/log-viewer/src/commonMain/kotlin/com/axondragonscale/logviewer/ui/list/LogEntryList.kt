package com.axondragonscale.logviewer.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.ui.logRowTime
import com.axondragonscale.logviewer.ui.style.LocalLogStyle
import com.axondragonscale.logviewer.ui.style.colors

/** Created by Ronak Harkhani on 11/22/26 */

/** Newest at the bottom, like a console: the reversed layout puts index 0 there. */
@Composable
internal fun LogEntryList(
    listState: LazyListState = rememberLazyListState(),
    entries: List<LogRecord>,
    hasOlder: Boolean,
    wrap: Boolean,
    onSelect: (LogRecord) -> Unit,
    onLoadOlder: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        reverseLayout = true,
    ) {
        items(items = entries, key = { it.id.value }, contentType = { it.level }) { entry ->
            LogEntryRow(entry = entry, wrap = wrap, onClick = { onSelect(entry) })
        }
        if (hasOlder) {
            item(key = "older") {
                // Reversed, this footer sits above the oldest row: composing it means the tail is close.
                LaunchedEffect(entries.size) { onLoadOlder() }
                OlderFooter()
            }
        }
    }
}

@Composable
private fun LogEntryRow(
    entry: LogRecord,
    wrap: Boolean,
    onClick: () -> Unit,
) {
    val style = LocalLogStyle.current
    val colors = entry.level.colors()
    val trace = entry.message.lineSequence().drop(1).toList()

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
                text = entry.time.logRowTime(),
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
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
        if (trace.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(style.traceSurface)
                    .padding(8.dp),
            ) {
                LogTrace(
                    lines = trace,
                    style = style.mono,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (wrap) 12 else 6,
                )
            }
        }
    }
}

@Composable
private fun OlderFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Loading older…",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
