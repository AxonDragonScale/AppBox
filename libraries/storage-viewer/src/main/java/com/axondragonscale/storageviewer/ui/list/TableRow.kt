package com.axondragonscale.storageviewer.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val RowHeight = 56.dp // design: rowStore56, grown from rowList48 for the 32 dp actions
private val GlyphSize = 18.dp
private val RowGap = 10.dp
private val RowPadding = 16.dp
private val ActionHeight = 32.dp
private val ActionRadius = 8.dp
private val ActionMinWidth = 56.dp
private val ActionGap = 8.dp
private val ActionPadding = 10.dp
private val ActionBorder = 1.dp

/**
 * One table inside a database: the name reads mono because it is an identifier, the counts because
 * they are the table's shape. The body opens the definition; the two actions go straight to it or
 * to the records.
 */
@Composable
internal fun StorageTableRow(
    modifier: Modifier = Modifier,
    name: String,
    counts: String,
    icon: ImageVector,
    onClick: () -> Unit,
    onDdl: () -> Unit,
    onRows: () -> Unit,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(GlyphSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = style.monoValue,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = counts,
                style = style.monoChip,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(ActionGap)) {
            TableAction(label = "DDL", onClick = onDdl)
            TableAction(label = "Rows", onClick = onRows)
        }
    }
}

/** A 32 dp outlined action that still reports a 48 dp touch target. */
@Composable
private fun TableAction(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .height(ActionHeight)
            .widthIn(min = ActionMinWidth)
            .minimumInteractiveComponentSize(),
        shape = RoundedCornerShape(ActionRadius),
        border = BorderStroke(ActionBorder, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = PaddingValues(horizontal = ActionPadding),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}
