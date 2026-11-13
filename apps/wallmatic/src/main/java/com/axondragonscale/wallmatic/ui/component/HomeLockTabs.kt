package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.model.TargetScreen
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun HomeLockTabs(
    modifier: Modifier = Modifier,
    selected: TargetScreen,
    onSelect: (TargetScreen) -> Unit,
) {
    RuledTabs(
        modifier = modifier,
        items = listOf(TargetScreen.Home, TargetScreen.Lock),
        selected = selected,
        onSelect = onSelect,
        height = 40.dp,
        indicator = RuledIndicator.Bottom,
        border = true,
        dividerColor = MaterialTheme.wallmatic.ink,
        role = Role.Tab,
    ) { target, isSelected ->
        Text(
            text = target.label,
            style = WallmaticTextStyles.microStrong,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) {
                MaterialTheme.wallmatic.ink
            } else {
                MaterialTheme.wallmatic.inkMuted
            },
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeLockTabsPreview() {
    WallmaticTheme {
        var selected by remember { mutableStateOf(TargetScreen.Home) }
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper).padding(24.dp)) {
            HomeLockTabs(selected = selected, onSelect = { selected = it })
        }
    }
}
