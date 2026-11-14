package com.axondragonscale.storageviewer.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.model.PreferenceEntry
import com.axondragonscale.storageviewer.ui.chrome.TypeChip
import com.axondragonscale.storageviewer.ui.chrome.preferenceColor
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val RowHeight = 48.dp // design: rowList48
private val KeyWidth = 150.dp // design: key column 150
private val TypeSlot = 64.dp // design: type column 64
private val ChevronSize = 16.dp
private val RowGap = 8.dp
private val RowPadding = 16.dp

/** One preference entry: key, type, value. The three columns hold their widths down the list. */
@Composable
internal fun KeyRow(
    modifier: Modifier = Modifier,
    entry: PreferenceEntry,
    onClick: () -> Unit,
) {
    val style = LocalStorageViewerStyle.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .minimumInteractiveComponentSize()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = RowPadding),
        horizontalArrangement = Arrangement.spacedBy(RowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = entry.key,
            modifier = Modifier.width(KeyWidth),
            style = style.monoKey,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier.width(TypeSlot),
            contentAlignment = Alignment.CenterStart,
        ) {
            TypeChip(text = entry.type.name, color = preferenceColor(entry.type))
        }
        Text(
            text = entry.display,
            modifier = Modifier.weight(1f),
            style = style.monoValue,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(ChevronSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
