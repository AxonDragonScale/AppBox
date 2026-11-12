package com.axondragonscale.wallmatic.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.wallmatic.ui.theme.Spacing
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import java.util.Locale

/**
 * Created by Ronak Harkhani on 12/11/26
 */

@Composable
fun SettingsRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = MaterialTheme.wallmatic

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 60.dp else 72.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent()

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = WallmaticTextStyles.rowTitle,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = WallmaticTextStyles.caption,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        trailing?.invoke()
    }
}

@Composable
fun SettingsSectionHeader(
    modifier: Modifier = Modifier,
    title: String,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(Locale.ROOT),
            style = WallmaticTextStyles.label,
            color = MaterialTheme.wallmatic.primary,
            modifier = Modifier.padding(bottom = Spacing.sm),
        )
        HorizontalRule(color = MaterialTheme.wallmatic.ink)
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsRowPreview() {
    WallmaticTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.wallmatic.paper)
                .padding(24.dp)
        ) {
            SettingsRow(
                title = "Mirror Home Screen",
                subtitle = "Use the same album on Lock",
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.SyncLock,
                        contentDescription = null,
                        tint = MaterialTheme.wallmatic.primary,
                    )
                },
                trailing = { WallmaticSwitch(checked = false, onCheckedChange = {}) },
            )
            HorizontalRule()
            SettingsRow(
                title = "Grid Size",
                subtitle = "Columns in the Grid",
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = null,
                        tint = MaterialTheme.wallmatic.primary,
                    )
                },
                trailing = {
                    WallmaticSegmented(
                        options = listOf(2, 3, 4),
                        selected = 2,
                        onSelect = {},
                        label = { it.toString() },
                    )
                },
            )
            SettingsSectionHeader(
                modifier = Modifier.padding(top = 24.dp),
                title = "About",
            )
        }
    }
}
