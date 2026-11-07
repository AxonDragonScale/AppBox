package com.axondragonscale.devinator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.axondragonscale.devinator.R

private const val TAGLINE = "Developer options, minus the hassle."

/**
 * The one top bar every screen uses: the Devinator mark beside the app name, with the tagline under
 * it and a slot for trailing actions (Home puts its settings button there).
 *
 * The design source is the `TopBar` component in `apps/devinator/devinator.fig`, so the bar is drawn
 * once there and instanced into the screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevinatorTopBar(
    modifier: Modifier = Modifier,
    subtitle: String? = TAGLINE,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_devinator_logo),
                    contentDescription = null,
                    modifier = Modifier.size(width = 28.dp, height = 16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(text = "Devinator", style = MaterialTheme.typography.titleLarge)
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}
