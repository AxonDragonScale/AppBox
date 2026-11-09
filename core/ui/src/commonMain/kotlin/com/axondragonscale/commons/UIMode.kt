package com.axondragonscale.commons

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Created by Ronak Harkhani on 23 January 2026.
 *
 * Common UI mode enum for controlling light/dark theme across all apps.
 */
enum class UIMode(val displayName: String) {
    LIGHT("Light"),
    AUTO("Auto"),
    DARK("Dark");

    companion object {
        val DEFAULT get() = AUTO

        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}

val UIMode.icon: ImageVector
    get() = when (this) {
        UIMode.LIGHT -> Icons.Default.LightMode
        UIMode.AUTO -> Icons.Default.BrightnessAuto
        UIMode.DARK -> Icons.Default.DarkMode
    }

/**
 * Resolves the [UIMode] to a boolean indicating whether dark theme should be used.
 * [UIMode.AUTO] delegates to the system's dark theme setting.
 */
@Composable
fun UIMode.isDarkTheme(): Boolean = when (this) {
    UIMode.LIGHT -> false
    UIMode.DARK -> true
    UIMode.AUTO -> isSystemInDarkTheme()
}
