package com.axondragonscale.topty.ui.devtools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.topty.BuildConfig
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Topty-specific developer tools screen.
 * Uses the common DevToolsScreen and adds Topty-specific tools.
 */
@Composable
fun ToptyDevTools(
    modifier: Modifier = Modifier,
) {
    val prefsRepository: ToptyPrefsRepository = koinInject()
    val scope = rememberCoroutineScope()

    DevToolsScreen(
        modifier = modifier,
        title = "Topty Dev Tools",
        subtitle = "Debug utilities for Topty",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)

            // Onboarding Section
            section("Onboarding", Icons.Rounded.Refresh)
            action(
                title = "Reset Onboarding",
                subtitle = "Clear onboarding completion flag to show onboarding again on next launch",
                onClick = {
                    scope.launch { prefsRepository.hasCompletedOnboarding.set(false) }
                }
            )
        }
    )
}
