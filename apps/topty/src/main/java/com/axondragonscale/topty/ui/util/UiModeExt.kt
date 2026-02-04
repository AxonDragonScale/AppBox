package com.axondragonscale.topty.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import com.axondragonscale.topty.model.UIMode

/**
 * Created by Ronak Harkhani on 29/10/24
 */

fun UIMode.getIcon(isActive: Boolean) = when (this) {
    UIMode.LIGHT -> if (isActive) Icons.Filled.LightMode else Icons.Outlined.LightMode
    UIMode.DARK -> if (isActive) Icons.Filled.DarkMode else Icons.Outlined.DarkMode
    UIMode.AUTO -> if (isActive) Icons.Filled.BrightnessAuto else Icons.Outlined.BrightnessAuto
}
