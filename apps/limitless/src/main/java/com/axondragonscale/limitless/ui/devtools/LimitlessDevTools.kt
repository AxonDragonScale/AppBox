package com.axondragonscale.limitless.ui.devtools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dataset
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.limitless.BuildConfig
import org.koin.compose.koinInject

/**
 * Limitless-specific developer tools screen.
 * Uses the common DevToolsScreen and adds Limitless-specific tools.
 */
@Composable
fun LimitlessDevTools(
    modifier: Modifier = Modifier,
    seeder: SampleDataSeeder = koinInject(),
) {
    DevToolsScreen(
        modifier = modifier,
        title = "Limitless Dev Tools",
        subtitle = "Debug utilities for Limitless",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)

            // Test Data Section
            section("Test Data", Icons.Rounded.Science)
            action(
                title = "Seed Sample Data",
                subtitle = "Create 4 habits with ~6 months of records",
                icon = Icons.Rounded.Dataset,
                completionAlert = "Sample data seeded successfully",
                onClick = { seeder.seed() },
            )
            action(
                title = "Clear All Habits",
                subtitle = "Delete all habits and their records",
                icon = Icons.Rounded.DeleteSweep,
                destructive = true,
                completionAlert = "All habits cleared",
                onClick = { seeder.clearAll() },
            )
        }
    )
}
