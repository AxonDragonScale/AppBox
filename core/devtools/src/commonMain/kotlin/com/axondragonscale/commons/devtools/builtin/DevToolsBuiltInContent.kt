package com.axondragonscale.commons.devtools.builtin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.devtools.model.DevTool
import com.axondragonscale.commons.devtools.ui.DevToolActionItem

/** The platform's own tools (kill, clear data, settings, permissions), at the top of the screen. */
@Composable
expect fun DevToolsBuiltInContent()

/** The one built-in every platform has; only what "kill" means differs. */
internal fun killAppTool(onClick: () -> Unit): DevTool.Action = DevTool.Action(
    title = "Kill App",
    subtitle = "Force stop the application",
    icon = Icons.Rounded.PowerSettingsNew,
    destructive = true,
    onClick = { onClick() },
)

/** What the targets without a platform tool list show: Kill App, whose meaning is [killApp]'s. */
@Composable
internal fun KillAppOnlyContent(killApp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DevToolActionItem(tool = killAppTool(killApp))
    }
}
