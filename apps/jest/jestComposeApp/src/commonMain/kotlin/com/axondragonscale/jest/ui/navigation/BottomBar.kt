package com.axondragonscale.jest.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.theme.DarkSurface
import com.axondragonscale.jest.ui.theme.JestTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Floating bottom tab bar for mobile (Compact width).
 * 3 destinations: Home, Library, Settings.
 * Active tab expands to show label; inactive tabs show icon only.
 * Matches the MobileTabBar component in jest.pen.
 */
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    destinations: List<Destination>,
    activeDestination: Destination,
    onDestinationSelected: (Destination) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = DarkSurface,
                shape = RoundedCornerShape(40.dp),
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        destinations.forEach { destination ->
            val isActive = destination == activeDestination
            val interactionSource = remember { MutableInteractionSource() }

            val weight by animateFloatAsState(
                targetValue = if (isActive) 3f else 1f,
                animationSpec = tween(durationMillis = 250),
            )
            val backgroundColor by animateColorAsState(
                targetValue = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 250),
            )
            val contentColor by animateColorAsState(
                targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 250),
            )

            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(weight)
                    .clip(RoundedCornerShape(40.dp))
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onDestinationSelected(destination) },
                    )
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = if (isActive) destination.activeIcon else destination.inactiveIcon,
                    contentDescription = destination.label,
                    tint = contentColor,
                )
                if (isActive) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = destination.label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun BottomBarPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 48.dp, vertical = 20.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            BottomBar(
                destinations = Destination.all,
                activeDestination = Destination.Home,
                onDestinationSelected = {},
            )
        }
    }
}
