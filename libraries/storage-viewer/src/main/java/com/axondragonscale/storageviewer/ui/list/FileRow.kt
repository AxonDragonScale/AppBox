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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.data.formatBytes
import com.axondragonscale.storageviewer.model.FileNode
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val RowHeight = 52.dp // design: rowFile52
private val GlyphSize = 18.dp
private val ChevronSize = 16.dp
private val RowGap = 10.dp
private val RowPadding = 16.dp

/**
 * One entry inside a files root. A folder carries no size because its size is only known once it
 * is walked; it adds the chevron that says it opens. [trailing] replaces the size where the caller
 * knows a count instead, as the root list does; [icon] lets a root row say it is an open root.
 */
@Composable
internal fun FileRow(
    modifier: Modifier = Modifier,
    node: FileNode,
    meta: String? = null,
    trailing: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val style = LocalStorageViewerStyle.current
    val end = trailing ?: if (node.isDirectory) null else formatBytes(node.sizeBytes)
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
        Icon(
            imageVector = icon ?: if (node.isDirectory) Icons.Filled.Folder else Icons.Filled.Description,
            contentDescription = null,
            modifier = Modifier.size(GlyphSize),
            tint = if (node.isDirectory) {
                style.typeFiles
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = node.name,
                style = style.monoValue,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (meta != null) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (end != null) {
            Text(
                text = end,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
        if (node.isDirectory) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(ChevronSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
