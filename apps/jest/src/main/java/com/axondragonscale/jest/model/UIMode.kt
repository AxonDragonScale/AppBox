package com.axondragonscale.jest.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Created by Ronak Harkhani on 09/06/24
 */

enum class UIMode(val displayName: String, val icon: ImageVector) {
    LIGHT("Light", Icons.Default.LightMode),
    AUTO("Auto", Icons.Default.BrightnessAuto),
    DARK("Dark", Icons.Default.DarkMode);

    companion object {
        val DEFAULT get() = AUTO

        fun fromOrdinal(ordinal: Int) = UIMode.entries[ordinal]
    }
}
