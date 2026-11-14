package com.axondragonscale.storageviewer.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle

/** Created by Ronak on 14/11/26 */

private val DefaultRowHeight = 64.dp
private val BarHeight = 10.dp
private val BarRadius = 4.dp
private val BarGap = 8.dp
private val RowPadding = 16.dp
private const val BarWide = 0.7f
private const val BarMiddle = 0.4f
private const val BarNarrow = 0.3f

/** The placeholder for a row that has not arrived; it borrows the height of the row it stands in. */
@Composable
internal fun SkeletonRow(
    modifier: Modifier = Modifier,
    height: Dp = DefaultRowHeight,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = RowPadding),
        verticalArrangement = Arrangement.spacedBy(BarGap, Alignment.CenterVertically),
    ) {
        SkeletonBar(widthFraction = BarMiddle)
        SkeletonBar(widthFraction = BarWide)
        SkeletonBar(widthFraction = BarNarrow)
    }
}

@Composable
internal fun SkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 3,
    rowHeight: Dp = DefaultRowHeight,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(count) { SkeletonRow(height = rowHeight) }
    }
}

@Composable
private fun SkeletonBar(widthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(BarHeight)
            .clip(RoundedCornerShape(BarRadius))
            .background(LocalStorageViewerStyle.current.skeletonFill),
    )
}
