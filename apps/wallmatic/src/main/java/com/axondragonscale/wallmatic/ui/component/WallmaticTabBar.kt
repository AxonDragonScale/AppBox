package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.axondragonscale.wallmatic.ui.bottombar.Tab
import com.axondragonscale.wallmatic.ui.theme.Dimens
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun WallmaticTabBar(
    modifier: Modifier = Modifier,
    active: Tab,
    onSelect: (Tab) -> Unit,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dividerColor = MaterialTheme.wallmatic.ink

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.tabBarHeight + bottomInset)
            .background(MaterialTheme.wallmatic.paper)
            .drawBehind {
                // Dividers span the inset strip too, so they run to the window edge.
                val dividerWidth = Dimens.rule.toPx()
                val cellWidth = (size.width - 2 * dividerWidth) / 3
                drawRect(dividerColor, Offset(cellWidth, 0f), Size(dividerWidth, size.height))
                drawRect(dividerColor, Offset(2 * cellWidth + dividerWidth, 0f), Size(dividerWidth, size.height))
            }
    ) {
        HorizontalRule(color = MaterialTheme.wallmatic.ink)

        RuledTabs(
            items = Tab.entries,
            selected = active,
            onSelect = onSelect,
            height = Dimens.tabBarHeight - Dimens.rule,
            indicator = RuledIndicator.Top,
            border = false,
            dividerColor = Color.Transparent,
            role = Role.Tab,
        ) { tab, isSelected ->
            Text(
                text = tab.label,
                style = WallmaticTextStyles.microStrong,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) {
                    MaterialTheme.wallmatic.ink
                } else {
                    MaterialTheme.wallmatic.inkMuted
                },
            )
        }

        Spacer(modifier = Modifier.height(bottomInset))
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WallmaticTabBarPreview() {
    WallmaticTheme {
        var active by remember { mutableStateOf(Tab.Albums) }
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper)) {
            WallmaticTabBar(active = active, onSelect = { active = it })
        }
    }
}
