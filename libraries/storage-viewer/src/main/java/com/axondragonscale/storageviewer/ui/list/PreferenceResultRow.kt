package com.axondragonscale.storageviewer.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
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
import com.axondragonscale.storageviewer.ui.PreferenceHit
import com.axondragonscale.storageviewer.ui.chrome.TypeChip
import com.axondragonscale.storageviewer.ui.chrome.preferenceColor
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val RowHeight = 64.dp // design: rowData64
private val ChevronSize = 16.dp
private val RowGap = 8.dp
private val RowPadding = 16.dp
private val LineGap = 2.dp
private val ChipGap = 8.dp

/**
 * One cross-store search hit (design 18): the key with its type, then the store and the value that
 * matched. The store name leads line two, so a result never hides where it came from.
 */
@Composable
internal fun PreferenceResultRow(
    modifier: Modifier = Modifier,
    hit: PreferenceHit,
    onClick: () -> Unit,
) {
    val style = LocalStorageViewerStyle.current
    val entry = hit.entry
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LineGap)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ChipGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.key,
                    modifier = Modifier.weight(1f, fill = false),
                    style = style.monoValue,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TypeChip(text = entry.type.name, color = preferenceColor(entry.type))
            }
            Text(
                text = "${hit.store.name} · ${entry.display}",
                style = style.monoChip,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(ChevronSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
