package com.axondragonscale.storageviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Created by Ronak on 14/11/26 */

private val TopBarHeight = 64.dp // design: topBar64
private val SlotSize = 44.dp // a 44 dp icon slot inside the 48 dp target
private val SlotRadius = 12.dp
private val SlotIconSize = 22.dp
private val EdgeInset = 12.dp // 16 side padding, less the slot's own optical inset

/**
 * One bar for every surface. A root has no back and no overflow; a detail surface adds the back
 * slot, and the trailing slots stay in the order refresh then overflow.
 */
@Composable
internal fun StorageTopBar(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onBack: (() -> Unit)?,
    onRefresh: (() -> Unit)?,
    onOverflow: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TopBarHeight)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = EdgeInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TopBarSlot(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "Back",
                onClick = onBack,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        actions()
        if (onRefresh != null) {
            TopBarSlot(icon = Icons.Filled.Refresh, label = "Refresh", onClick = onRefresh)
        }
        if (onOverflow != null) {
            TopBarSlot(icon = Icons.Filled.MoreVert, label = "More", onClick = onOverflow)
        }
    }
}

@Composable
private fun TopBarSlot(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(SlotSize)
            .clip(RoundedCornerShape(SlotRadius))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(SlotIconSize),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
