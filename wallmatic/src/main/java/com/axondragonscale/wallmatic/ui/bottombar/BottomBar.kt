package com.axondragonscale.wallmatic.ui.bottombar

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme

/**
 * Created by Ronak Harkhani on 06/06/24
 */

val BOTTOM_BAR_HEIGHT = 64.dp
private val BottomBarShape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40)

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    tabs: List<Tab>,
    activeTab: Tab,
    onTabChange: (Tab) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BOTTOM_BAR_HEIGHT)
            .shadow(elevation = 20.dp, shape = BottomBarShape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = BottomBarShape
            )
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        tabs.forEach { tab ->
            val isActive = tab == activeTab
            val interactionSource = remember { MutableInteractionSource() }
            val weight by animateFloatAsState(targetValue = if (isActive) 3f else 1f)
            val contentColor by animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
            val backgroundColor by animateColorAsState(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer
            )
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onTabChange(tab)
                    }
                    .padding(8.dp)
                    .weight(weight)
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(40)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (isActive) tab.activeIcon else tab.inactiveIcon,
                    contentDescription = null,
                    tint = contentColor
                )


                AnimatedVisibility(isActive) {
                    Row {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tab.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    WallmaticTheme {
        var currentTab by remember { mutableStateOf<Tab>(Tab.Home) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomBar(
                modifier = Modifier,
                activeTab = currentTab,
                tabs = listOf(Tab.Home, Tab.Albums, Tab.Settings),
                onTabChange = { currentTab = it }
            )
        }
    }
}
