package com.axondragonscale.storageviewer.ui.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val TabRowHeight = 44.dp // design: a 40 dp track inside a 44 dp hit row
private val TabTrackInset = 2.dp
private val TabSideInset = 4.dp
private val TabGap = 6.dp
private val TabRadius = 999.dp // design: pill999
private val CountChipHeight = 18.dp
private val CountChipPadding = 4.dp
private val SegmentPadding = 6.dp
private val LabelToCount = 4.dp
private const val ActiveBorderAlpha = 0.5f // primaryContainerOutline has no Material role

@Immutable
internal data class TabSegment(val label: String, val count: Int)

/**
 * The three store segments. Equal-width pills, so the row keeps its geometry at 380 dp with a
 * two-digit count on every segment.
 */
@Composable
internal fun StorageSegmentedTabs(
    modifier: Modifier = Modifier,
    segments: List<TabSegment>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TabRowHeight)
            .padding(horizontal = TabSideInset, vertical = TabTrackInset),
        horizontalArrangement = Arrangement.spacedBy(TabGap),
    ) {
        segments.forEachIndexed { index, segment ->
            Segment(
                segment = segment,
                active = index == selected,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun RowScope.Segment(
    segment: TabSegment,
    active: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(TabRadius)
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(shape)
            .background(if (active) scheme.primaryContainer else Color.Transparent)
            .then(
                if (active) {
                    Modifier.border(1.dp, scheme.primary.copy(alpha = ActiveBorderAlpha), shape)
                } else {
                    Modifier
                },
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = SegmentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = segment.label,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.labelLarge,
            color = if (active) scheme.primary else scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
        )
        Spacer(Modifier.width(LabelToCount))
        CountChip(count = segment.count)
    }
}

@Composable
private fun CountChip(count: Int) {
    val style = LocalStorageViewerStyle.current
    Box(
        modifier = Modifier
            .height(CountChipHeight)
            .clip(RoundedCornerShape(TabRadius))
            .background(style.chipFill)
            .padding(horizontal = CountChipPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = style.monoChip,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
        )
    }
}
