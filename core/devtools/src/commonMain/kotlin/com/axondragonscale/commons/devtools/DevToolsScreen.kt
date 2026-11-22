package com.axondragonscale.commons.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.devtools.builtin.DevToolsBuiltInContent
import com.axondragonscale.commons.devtools.host.DevToolsHostWindow
import com.axondragonscale.commons.devtools.host.devToolsHosts
import com.axondragonscale.commons.devtools.model.DevTool
import com.axondragonscale.commons.devtools.model.DevToolsBuilder
import com.axondragonscale.commons.devtools.model.buildDevTools
import com.axondragonscale.commons.devtools.ui.DevToolItem
import com.axondragonscale.commons.devtools.ui.DevToolsSurface

/**
 * The developer tools screen: the platform's built-in tools, the viewers it can host, then [tools].
 */
@Composable
fun DevToolsScreen(
    modifier: Modifier = Modifier,
    title: String = "Developer Tools",
    subtitle: String? = "Debug utilities and settings",
    tools: DevToolsBuilder.() -> Unit = {},
) {
    val hosts = devToolsHosts.filter { it.available() }
    var openHost by rememberSaveable { mutableStateOf<String?>(null) }

    DevToolsSurface {
        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    DevToolsHeader(title = title, subtitle = subtitle)
                }

                item {
                    DevToolItem(
                        tool = DevTool.Section(
                            title = "App Actions",
                            icon = Icons.Rounded.PowerSettingsNew,
                        )
                    )
                }

                item {
                    DevToolsBuiltInContent()
                }

                if (hosts.isNotEmpty()) {
                    item {
                        DevToolItem(
                            tool = DevTool.Section(
                                title = "Diagnostics",
                                icon = Icons.Rounded.Terminal,
                            )
                        )
                    }
                    items(hosts, key = { it.id }) { host ->
                        DevToolItem(
                            tool = DevTool.Action(
                                title = host.title,
                                subtitle = host.subtitle,
                                icon = host.icon,
                                onClick = { openHost = host.id },
                            )
                        )
                    }
                }

                items(buildDevTools(tools)) { tool ->
                    DevToolItem(tool = tool)
                }
            }

            DevToolsHostWindow(
                host = hosts.firstOrNull { it.id == openHost },
                onClose = { openHost = null },
            )
        }
    }
}

@Composable
private fun DevToolsHeader(
    title: String,
    subtitle: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
