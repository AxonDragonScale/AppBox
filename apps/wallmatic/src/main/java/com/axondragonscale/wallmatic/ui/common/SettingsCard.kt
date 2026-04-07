package com.axondragonscale.wallmatic.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.axondragonscale.wallmatic.ui.util.thenIf

/**
 * Created by Ronak Harkhani on 29/09/24
 */

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier.thenIf(onClick != null) { clickable(onClick = onClick!!) },
        leadingContent = leadingContent,
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            leadingIconColor = MaterialTheme.colorScheme.primary,
            headlineColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
