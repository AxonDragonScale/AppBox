package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun <T> WallmaticSegmented(
    modifier: Modifier = Modifier,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
) {
    RuledTabs(
        modifier = modifier,
        items = options,
        selected = selected,
        onSelect = onSelect,
        height = 32.dp,
        indicator = RuledIndicator.Bottom,
        border = true,
        role = Role.RadioButton,
        width = 128.dp,
    ) { option, isSelected ->
        Text(
            text = label(option),
            style = segmentTextStyle(isSelected),
            color = if (isSelected) {
                MaterialTheme.wallmatic.ink
            } else {
                MaterialTheme.wallmatic.inkMuted
            },
        )
    }
}

@Composable
fun WallmaticIconSegmented(
    modifier: Modifier = Modifier,
    icons: List<ImageVector>,
    contentDescriptions: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    RuledTabs(
        modifier = modifier,
        items = icons.indices.toList(),
        selected = selectedIndex,
        onSelect = onSelect,
        height = 32.dp,
        indicator = RuledIndicator.Bottom,
        border = true,
        role = Role.RadioButton,
        width = 128.dp,
    ) { index, isSelected ->
        Icon(
            imageVector = icons[index],
            contentDescription = contentDescriptions.getOrNull(index),
            tint = if (isSelected) {
                MaterialTheme.wallmatic.ink
            } else {
                MaterialTheme.wallmatic.inkMuted
            },
            modifier = Modifier.size(16.dp),
        )
    }
}

private fun segmentTextStyle(selected: Boolean) = WallmaticTextStyles.micro.copy(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
)

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SegmentedPreview() {
    WallmaticTheme {
        Column(modifier = Modifier.background(MaterialTheme.wallmatic.paper).padding(24.dp)) {
            WallmaticSegmented(
                options = listOf(2, 3, 4),
                selected = 2,
                onSelect = {},
                label = { it.toString() },
            )
            WallmaticIconSegmented(
                modifier = Modifier.padding(top = 16.dp),
                icons = listOf(Icons.Filled.LightMode, Icons.Filled.BrightnessAuto, Icons.Filled.DarkMode),
                contentDescriptions = listOf("Light", "System", "Dark"),
                selectedIndex = 1,
                onSelect = {},
            )
        }
    }
}
