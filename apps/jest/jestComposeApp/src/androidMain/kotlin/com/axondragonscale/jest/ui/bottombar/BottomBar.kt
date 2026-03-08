package com.axondragonscale.jest.ui.bottombar

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.theme.JestTheme

@Composable
fun BottomTabBar(
    modifier: Modifier = Modifier,
    tabs: List<Tab>,
    activeTab: Tab,
    onTabChange: (Tab) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(40)
            )
            .padding(4.dp),
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
                    .padding(4.dp)
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
private fun BottomBarPreview() {
    JestTheme {
        Box(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomTabBar(
                modifier = Modifier.padding(horizontal = 56.dp, vertical = 16.dp),
                activeTab = Tab.Home,
                tabs = listOf(Tab.Home, Tab.Favorites, Tab.History),
                onTabChange = { }
            )
        }
    }
}
