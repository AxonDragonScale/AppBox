package com.axondragonscale.wallmatic.ui.devtools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.devtools.DevTool
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.wallmatic.BuildConfig
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/**
 * Wallmatic-specific developer tools screen.
 * Uses the common DevToolsScreen and adds Wallmatic-specific tools.
 */
@Composable
fun WallmaticDevTools(
    modifier: Modifier = Modifier,
    appPrefsRepository: AppPrefsRepository = koinInject(),
    syncManager: SyncManager = koinInject(),
) {
    val fastAutoCycle by appPrefsRepository.fastAutoCycleFlow
        .collectAsStateWithLifecycle(initialValue = false)

    DevToolsScreen(
        modifier = modifier,
        title = "Wallmatic Dev Tools",
        subtitle = "Debug utilities for Wallmatic",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)

            // Wallmatic Tools Section
            section("Wallmatic Tools", Icons.Rounded.Sync)

            action(
                title = "Sync Albums",
                subtitle = "Synchronize all albums with filesystem",
                icon = Icons.Rounded.Sync,
                onClick = { withContext(Dispatchers.IO) { syncManager.syncAlbums() } }
            )

            toggle(
                title = "Fast Auto-Cycle",
                subtitle = "Use 1 minute interval instead of configured duration",
                icon = Icons.Rounded.Timer,
                checked = fastAutoCycle,
            ) { enabled ->
                CoroutineScope(Dispatchers.IO).launch {
                    appPrefsRepository.setFastAutoCycle(enabled)
                }
            }
        }
    )
}
