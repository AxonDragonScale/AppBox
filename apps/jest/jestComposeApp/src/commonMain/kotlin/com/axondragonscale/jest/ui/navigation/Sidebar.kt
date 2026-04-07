package com.axondragonscale.jest.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.theme.JestTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Sidebar navigation rail for Tablet (240dp) and Desktop (280dp) form factors.
 * Matches the Sidebar / SidebarNavItem components in jest.pen.
 */
@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    destinations: List<Destination>,
    activeDestination: Destination,
    onDestinationSelected: (Destination) -> Unit,
    width: Dp = 240.dp,
) {
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            SidebarBranding()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                destinations.forEach { destination ->
                    SidebarNavItem(
                        destination = destination,
                        isActive = destination == activeDestination,
                        onClick = { onDestinationSelected(destination) },
                    )
                }
            }
        }

        // Right border divider — 1dp line matching design token #2A2A2E
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outline),
        )
    }
}

@Composable
private fun SidebarBranding(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "🃏",
            style = MaterialTheme.typography.headlineMedium,
        )
        Column {
            Text(
                text = "Jest",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Daily Laughs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SidebarNavItem(
    modifier: Modifier = Modifier,
    destination: Destination,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primaryContainer
        else androidx.compose.ui.graphics.Color.Transparent,
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = if (isActive) destination.activeIcon else destination.inactiveIcon,
            contentDescription = destination.label,
            tint = contentColor,
        )
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
        )
    }
}

@Preview
@Composable
private fun SidebarPreview() {
    JestTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .height(600.dp)
        ) {
            Sidebar(
                destinations = Destination.all,
                activeDestination = Destination.Home,
                onDestinationSelected = {},
            )
        }
    }
}
