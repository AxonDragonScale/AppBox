package com.axondragonscale.qrqanvas.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import com.axondragonscale.qrqanvas.model.QRContent

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * Shared utility functions for resolving display metadata from [QRContent] instances.
 * Used across Home, Detail, History, and Scanner screens.
 */

/**
 * Resolves a Material icon for the given [QRContent] type.
 */
fun QRContent.typeIcon(): ImageVector = when (this) {
    is QRContent.Text -> Icons.Filled.TextFields
    is QRContent.Url -> Icons.Filled.Link
    is QRContent.WiFi -> Icons.Filled.Wifi
    is QRContent.Email -> Icons.Filled.Email
    is QRContent.Phone -> Icons.Filled.Phone
    is QRContent.Sms -> Icons.Filled.Sms
    is QRContent.Contact -> Icons.Filled.Person
    is QRContent.Location -> Icons.Filled.LocationOn
}

/**
 * Returns a human-readable label for the given [QRContent] type.
 */
fun QRContent.typeLabel(): String = when (this) {
    is QRContent.Text -> "Text"
    is QRContent.Url -> "URL"
    is QRContent.WiFi -> "Wi-Fi"
    is QRContent.Email -> "Email"
    is QRContent.Phone -> "Phone"
    is QRContent.Sms -> "SMS"
    is QRContent.Contact -> "Contact"
    is QRContent.Location -> "Location"
}

/**
 * Returns a human-readable display value summarizing the given [QRContent].
 */
fun QRContent.displayValue(): String = when (this) {
    is QRContent.Text -> value
    is QRContent.Url -> url
    is QRContent.WiFi -> buildString {
        appendLine("SSID: $ssid")
        append("Security: ${encryptionType.protocol}")
        if (hidden) append("\nHidden network")
    }
    is QRContent.Email -> buildString {
        appendLine(address)
        subject?.let { appendLine("Subject: $it") }
        body?.let { append("Body: $it") }
    }.trimEnd()
    is QRContent.Phone -> number
    is QRContent.Sms -> buildString {
        appendLine(number)
        message?.let { append(it) }
    }.trimEnd()
    is QRContent.Contact -> buildString {
        appendLine(name)
        phone?.let { appendLine("Phone: $it") }
        email?.let { appendLine("Email: $it") }
        organization?.let { append("Org: $it") }
    }.trimEnd()
    is QRContent.Location -> buildString {
        appendLine("$latitude, $longitude")
        label?.let { append(it) }
    }.trimEnd()
}
