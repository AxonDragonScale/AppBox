package com.axondragonscale.devinator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Created by Ronak on 08/11/26 */

/**
 * A rounded container for a leading icon. The default tint is `onSurfaceVariant`: the badge is
 * illustration for the label beside it, so it must never compete with the row's title. Call sites
 * that carry meaning in colour (the hero card's state) pass their own.
 */

@Composable
fun IconBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 12.dp,
    containerColor: Color? = null,
    contentColor: Color? = null,
) = IconBadge(
    modifier = modifier,
    painter = rememberVectorPainter(icon),
    size = size,
    iconSize = iconSize,
    cornerRadius = cornerRadius,
    containerColor = containerColor,
    contentColor = contentColor,
)

@Composable
fun IconBadge(
    modifier: Modifier = Modifier,
    painter: Painter,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 12.dp,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = containerColor ?: MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(cornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = contentColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
