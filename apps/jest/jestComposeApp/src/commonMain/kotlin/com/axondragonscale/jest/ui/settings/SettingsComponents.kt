package com.axondragonscale.jest.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.jest.ui.theme.DarkBorder
import com.axondragonscale.jest.ui.theme.DarkSurfaceVariant
import com.axondragonscale.jest.ui.theme.DarkTextSecondary
import com.axondragonscale.jest.ui.theme.DarkTextTertiary
import com.axondragonscale.jest.ui.theme.JestTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Created by Ronak Harkhani on 24/10/24
 *
 * Reusable settings row components matching the jest.pen design.
 * All components use the dark surface (#16161A) card system with DM Sans typography.
 */

// region SettingsToggleRow

/**
 * A settings row with a leading icon, title, optional description, and a trailing [Switch].
 * Height: 64dp. Matches the SettingsToggleRow component in jest.pen.
 */
@Composable
fun SettingsToggleRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsIconBox(icon = icon, tint = iconTint)
            SettingsTextColumn(title = title, description = description)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = DarkSurfaceVariant,
                uncheckedBorderColor = DarkBorder,
            ),
        )
    }
}

// endregion

// region SettingsLinkRow

/**
 * A settings row with a leading icon, title, optional description, and a trailing chevron.
 * Height: 60dp. Matches the SettingsLinkRow component in jest.pen.
 * Optionally clickable if [onClick] is provided.
 */
@Composable
fun SettingsLinkRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.secondary,
    title: String,
    description: String? = null,
    showChevron: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsIconBox(icon = icon, tint = iconTint, size = 32)
            SettingsTextColumn(title = title, description = description)
        }
        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = DarkTextSecondary,
            )
        }
    }
}

// endregion

// region SettingsDivider

/**
 * A thin 1dp horizontal divider line within settings cards.
 * Matches the SettingsDivider component in jest.pen (#2A2A2E color).
 */
@Composable
fun SettingsDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(17.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkBorder),
        )
    }
}

// endregion

// region Internal helpers

@Composable
private fun SettingsIconBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    size: Int = 34,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(if (size >= 34) 10.dp else 8.dp))
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size((size * 0.47f).dp),
            imageVector = icon,
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
private fun SettingsTextColumn(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (description != null) 1.dp else 0.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = DarkTextTertiary,
            )
        }
    }
}

// endregion

// region Previews

@Preview
@Composable
private fun SettingsToggleRowPreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Column {
                SettingsToggleRow(
                    icon = Icons.Default.WbSunny,
                    title = "Dark Mode",
                    description = "Easy on the eyes",
                    checked = true,
                    onCheckedChange = {},
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.WbSunny,
                    title = "Dynamic Theme",
                    description = "Use Material You",
                    checked = false,
                    onCheckedChange = {},
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsLinkRowPreview() {
    JestTheme(darkTheme = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Column {
                SettingsLinkRow(
                    icon = Icons.Default.Settings,
                    title = "Open Dev Tools",
                    description = "Debug utilities and settings",
                    onClick = {},
                )
                SettingsDivider()
                SettingsLinkRow(
                    icon = Icons.Default.Settings,
                    title = "Jest",
                    description = "Version 1.0",
                    showChevron = false,
                )
            }
        }
    }
}

// endregion
