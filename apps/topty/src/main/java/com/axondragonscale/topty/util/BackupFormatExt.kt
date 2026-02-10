package com.axondragonscale.topty.util

import com.axondragonscale.topty.backup.BackupFormat
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 06 February 2026.
 */

fun BackupFormat.generateSuggestedFileName(): String {
    val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedTime = localDateTime.format(LocalDateTime.Formats.ISO)
    val name = when (this) {
        BackupFormat.TOPTY -> "topty-format"
        BackupFormat.AEGIS -> "aegis-format"
        BackupFormat.OTP_AUTH_URI -> "otpauth-format"
    }
    return "backup-${name}-${formattedTime}.${this.fileExtension}"
}
