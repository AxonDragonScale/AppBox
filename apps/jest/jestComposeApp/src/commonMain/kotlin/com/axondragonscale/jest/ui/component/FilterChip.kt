package com.axondragonscale.jest.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.theme.JestTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Filter chip component matching the FilterChip component in jest.pen.
 * Dark surface (#16161A) with 1dp stroke border (#2A2A2E), leading icon,
 * label text, and a trailing chevron-down icon.
 */
@Composable
fun FilterChip(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    isActive: Boolean = false,
    selectedCount: Int = 0,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(10.dp)
    val active = isActive || selectedCount > 0

    val backgroundColor = if (active) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface

    val borderColor = if (active) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline

    val contentColor = if (active) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            modifier = Modifier.height(14.dp),
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = if (selectedCount > 0) "$label · $selectedCount" else label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
        )
        Icon(
            modifier = Modifier.height(14.dp),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = contentColor,
        )
    }
}

@Preview
@Composable
private fun FilterChipInactivePreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            FilterChip(
                label = "Category",
                icon = Icons.Default.LocalOffer,
                isActive = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun FilterChipActivePreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            FilterChip(
                label = "Category",
                icon = Icons.Default.LocalOffer,
                selectedCount = 2,
                onClick = {},
            )
        }
    }
}
