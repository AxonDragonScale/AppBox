package com.axondragonscale.qrqanvas.ui.creator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Created by Ronak Harkhani on 10/07/25
 */
enum class CreatorTab(
    val label: String,
    val icon: ImageVector,
) {
    Content("Content", Icons.Filled.TextFields),
    Dots("Dots", Icons.Filled.Circle),
    Eyes("Eyes", Icons.Filled.Visibility),
    Background("Background", Icons.Filled.FormatPaint),
    Logo("Logo", Icons.Filled.Image),
    Templates("Templates", Icons.Filled.Dashboard),
}
