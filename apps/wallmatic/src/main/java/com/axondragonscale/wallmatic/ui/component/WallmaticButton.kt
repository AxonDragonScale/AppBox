package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import java.util.Locale

/**
 * Created by Ronak Harkhani on 12/11/26
 */

enum class WallmaticButtonStyle { Filled, Outline, Text }

@Composable
fun WallmaticButton(
    modifier: Modifier = Modifier,
    text: String,
    style: WallmaticButtonStyle = WallmaticButtonStyle.Filled,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.wallmatic
    val container = when (style) {
        WallmaticButtonStyle.Filled -> colors.primary
        WallmaticButtonStyle.Outline -> Color.Transparent
        WallmaticButtonStyle.Text -> Color.Transparent
    }
    val content = when (style) {
        WallmaticButtonStyle.Filled -> colors.onPrimary
        WallmaticButtonStyle.Outline -> colors.ink
        WallmaticButtonStyle.Text -> colors.primary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (style == WallmaticButtonStyle.Text) 40.dp else 48.dp)
            .background(container)
            .then(
                if (style == WallmaticButtonStyle.Outline) {
                    Modifier.border(1.dp, colors.ink)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text.uppercase(Locale.ROOT),
            style = WallmaticTextStyles.button,
            color = if (enabled) content else content.copy(alpha = 0.4f),
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WallmaticButtonPreview() {
    WallmaticTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.wallmatic.paper)
                .padding(24.dp),
        ) {
            WallmaticButton(text = "Change album", onClick = {})
            WallmaticButton(
                modifier = Modifier.padding(top = 16.dp),
                text = "Open album",
                style = WallmaticButtonStyle.Outline,
                onClick = {},
            )
            WallmaticButton(
                modifier = Modifier.padding(top = 16.dp),
                text = "Custom interval",
                style = WallmaticButtonStyle.Text,
                onClick = {},
            )
        }
    }
}
