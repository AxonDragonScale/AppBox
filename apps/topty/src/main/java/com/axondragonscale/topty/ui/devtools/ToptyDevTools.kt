package com.axondragonscale.topty.ui.devtools

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.topty.BuildConfig
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.ui.theme.ToptyTheme
import kotlinx.coroutines.launch
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
    val tokenRepository: TokenRepository = koinInject()
    val scope = rememberCoroutineScope()
    ToptyDevTools(
        modifier = modifier,
        onResetOnboarding = {
            scope.launch { prefsRepository.hasCompletedOnboarding.set(false) }
        },
        onAddTestData = {
            scope.launch { tokenRepository.saveTokens(getTestTokens()) }
        },
        onDeleteAllTokens = {
            scope.launch { tokenRepository.deleteAllTokens() }
        }
    )
}

@Composable
private fun ToptyDevTools(
    modifier: Modifier = Modifier,
    onResetOnboarding: () -> Unit,
    onAddTestData: () -> Unit,
    onDeleteAllTokens: () -> Unit,
) {
    DevToolsScreen(
        modifier = modifier,
        title = "Topty Dev Tools",
        subtitle = "Debug utilities for Topty",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)

            section("Dev Testing", Icons.Default.TouchApp)
            action(
                icon = Icons.Default.Refresh,
                title = "Reset Onboarding",
                subtitle = "Show onboarding again on next launch",
                destructive = true,
                onClick = onResetOnboarding,
                completionAlert = "Onboarding reset! Restart the app to see it."
            )
            action(
                icon = Icons.Default.Add,
                title = "Add Test Tokens",
                subtitle = "Add 10 diverse tokens for testing",
                onClick = onAddTestData,
                completionAlert = "Test tokens added! Check your token list."
            )
            action(
                icon = Icons.Default.Delete,
                title = "Clear All Tokens",
                subtitle = "Remove all tokens from the database",
                destructive = true,
                onClick = onDeleteAllTokens,
                completionAlert = "All tokens deleted! Your token list is now empty."
            )
        }
    )
}



@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ToptyDevToolsPreview() {
    ToptyTheme {
        Surface {
            ToptyDevTools(
                onResetOnboarding = {},
                onAddTestData = {},
                onDeleteAllTokens = {}
            )
        }
    }
}
