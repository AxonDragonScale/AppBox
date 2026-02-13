package com.axondragonscale.qrqanvas.ui.creator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Created by Ronak Harkhani on 13/02/26
 */
enum class ContentType(
    val label: String,
    val icon: ImageVector,
) {
    Text("Text", Icons.Filled.TextFields),
    Url("URL", Icons.Filled.Link),
    WiFi("WiFi", Icons.Filled.Wifi),
    Email("Email", Icons.Filled.Email),
    Phone("Phone", Icons.Filled.Phone),
    Sms("SMS", Icons.Filled.Sms),
}
