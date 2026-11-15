package com.axondragonscale.verso.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.axondragonscale.verso.ui.theme.Dimens
import com.axondragonscale.verso.ui.theme.VersoTextStyles
import com.axondragonscale.verso.ui.theme.verso

enum class Tab(val label: String) {
    Library("LIBRARY"),
    Stats("STATS"),
    Settings("SETTINGS"),
}

/**
 * Three segments, one shared language: a 1 dp track and a full-width ink rule on the active segment.
 */
@Composable
fun VersoTabBar(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.verso

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.paperSunk)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = colors.rule, thickness = Dimens.rule)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.tabBarHeight)
        ) {
            Tab.entries.forEach { tab ->
                val active = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.activeRule)
                            .background(if (active) colors.ink else Color.Transparent)
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.label,
                            style = VersoTextStyles.label,
                            color = if (active) colors.ink else colors.inkMuted,
                        )
                    }
                }
            }
        }
    }
}
