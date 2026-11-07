package com.axondragonscale.devinator.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.axondragonscale.devinator.ui.theme.LocalDevinatorColors

/**
 * The app's one switch. Drawn by hand rather than taken from Material so the off state can carry the
 * error palette: `dangerOff` is what the developer-options switch turns into when it is off.
 */
@Composable
fun DevinatorSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    large: Boolean = false,
    dangerOff: Boolean = false,
) {
    val trackWidth = if (large) 60.dp else 52.dp
    val trackHeight = if (large) 36.dp else 32.dp
    val thumbSize = if (large) 28.dp else 24.dp
    val iconSize = if (large) 16.dp else 14.dp
    val padding = 4.dp

    val trackColor = when {
        checked -> MaterialTheme.colorScheme.primary
        dangerOff -> LocalDevinatorColors.current.errorSurfaceHigh
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val thumbColor = when {
        checked -> MaterialTheme.colorScheme.onPrimary
        dangerOff -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconTint = when {
        checked -> MaterialTheme.colorScheme.primary
        dangerOff -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - padding * 2 else 0.dp,
        label = "switchThumbOffset",
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(trackWidth, trackHeight)
            .clip(RoundedCornerShape(trackHeight / 2))
            .background(trackColor)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(padding)
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center,
        ) {
            if (checked || dangerOff) {
                Icon(
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = iconTint,
                )
            }
        }
    }
}
