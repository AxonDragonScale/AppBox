package com.axondragonscale.storageviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val HeaderHeight = 40.dp // design: header40
private val CountPillHeight = 18.dp
private val CountPillPadding = 6.dp
private val PillRadius = 999.dp // design: pill999
private val ActionHitBox = 44.dp
private val ActionPadding = 8.dp
private val HeaderGap = 8.dp

/** A section's trailing text action; the label travels with its handler so they cannot disagree. */
@Immutable
internal data class SectionAction(val label: String, val onClick: () -> Unit)

/** The sticky label above a list, with its count pill and at most one trailing action. */
@Composable
internal fun StorageSectionHeader(
    modifier: Modifier = Modifier,
    label: String,
    count: String? = null,
    action: SectionAction? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HeaderHeight)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(HeaderGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (count != null) CountPill(count)
        Spacer(Modifier.weight(1f))
        if (action != null) {
            Box(
                modifier = Modifier
                    .height(ActionHitBox)
                    .clip(RoundedCornerShape(ActionPadding))
                    .clickable(role = Role.Button, onClick = action.onClick)
                    .padding(horizontal = ActionPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.primary,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CountPill(count: String) {
    val style = LocalStorageViewerStyle.current
    Box(
        modifier = Modifier
            .height(CountPillHeight)
            .clip(RoundedCornerShape(PillRadius))
            .background(style.chipFill)
            .padding(horizontal = CountPillPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count,
            style = style.monoChip,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
