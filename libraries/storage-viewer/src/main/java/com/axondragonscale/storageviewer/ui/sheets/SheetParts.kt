package com.axondragonscale.storageviewer.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Created by Ronak on 14/11/26 */

internal val SheetRadius = 24.dp // design: sheet24
internal val SheetPadding = 16.dp
internal val SheetHandleGap = 12.dp
internal val SheetActionHeight = 44.dp
internal val SheetActionRadius = 12.dp // design: tile12
internal const val SheetMaxFraction = 0.6f

private val HandleWidth = 40.dp
private val HandleHeight = 4.dp
private val CloseSlot = 40.dp
private val CloseIcon = 20.dp

/** The cap a sheet's scrolling body shares, so its actions never leave the screen. */
@Composable
internal fun sheetMaxHeight(): Dp {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    return (screenHeight * SheetMaxFraction).dp
}

/** The grab handle every sheet opens with. */
@Composable
internal fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = SheetHandleGap),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(HandleWidth)
                .height(HandleHeight)
                .clip(RoundedCornerShape(HandleHeight))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
        )
    }
}

/** The 40 dp dismiss slot in a sheet header; back and the scrim do the same thing. */
@Composable
internal fun SheetCloseButton(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .size(CloseSlot)
            .clip(RoundedCornerShape(SheetActionRadius))
            .clickable(role = Role.Button, onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "Close",
            modifier = Modifier.size(CloseIcon),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** One of a sheet's two actions; [primary] is the one the sheet exists for. */
@Composable
internal fun SheetAction(
    modifier: Modifier = Modifier,
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(SheetActionRadius)
    Box(
        modifier = modifier
            .height(SheetActionHeight)
            .clip(shape)
            .background(if (primary) scheme.primary else Color.Transparent)
            .border(1.dp, if (primary) scheme.primary else scheme.outline, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (primary) scheme.onPrimary else scheme.primary,
            maxLines = 1,
        )
    }
}
