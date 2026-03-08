package com.axondragonscale.commons.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.system.exitProcess

/**
 * Desktop (JVM) actual for [DevToolsBuiltInContent].
 *
 * Renders: Kill App only.
 */
@Composable
actual fun DevToolsBuiltInContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DevToolActionItem(
            tool = DevTool.Action(
                title = "Kill App",
                subtitle = "Force stop the application",
                icon = Icons.Rounded.PowerSettingsNew,
                destructive = true,
                onClick = { exitProcess(0) },
            )
        )
    }
}
