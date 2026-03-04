package com.axondragonscale.topty.util

import com.axondragonscale.topty.backup.BackupFormat
import com.axondragonscale.topty.backup.ToptyEncryptionOption
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Created by Ronak Harkhani on 06 February 2026.
 */

val BackupFormat.displayName: String
    get() = when (this) {
        is BackupFormat.Topty -> "Topty"
        is BackupFormat.Aegis -> "Aegis"
        is BackupFormat.OtpAuthUri -> "OTP Auth URI"
    }

val BackupFormat.description: String
    get() = when (this) {
        is BackupFormat.Topty -> "Topty's native format. Preserves all token data."
        is BackupFormat.Aegis -> "Compatible with Aegis, andOTP, 2FAS, and other apps."
        is BackupFormat.OtpAuthUri -> "Plain text otpauth:// URIs. Compatible with most apps."
    }

val BackupFormat.fileExtension: String
    get() = when (this) {
        is BackupFormat.Topty -> "topty"
        is BackupFormat.Aegis -> "json"
        is BackupFormat.OtpAuthUri -> "txt"
    }

val BackupFormat.mimeType: String
    get() = when (this) {
        is BackupFormat.Topty -> "application/octet-stream"
        is BackupFormat.Aegis -> "application/json"
        is BackupFormat.OtpAuthUri -> "text/plain"
    }

fun BackupFormat.generateSuggestedFileName(): String {
    val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedTime = localDateTime.format(LocalDateTime.Formats.ISO)
    val name = when (this) {
        is BackupFormat.Topty -> "topty-format"
        is BackupFormat.Aegis -> "aegis-format"
        is BackupFormat.OtpAuthUri -> "otpauth-format"
    }
    return "backup-${name}-${formattedTime}.${this.fileExtension}"
}

val ToptyEncryptionOption.displayName: String
    get() = when (this) {
        is ToptyEncryptionOption.AppPassword -> "App Password"
        is ToptyEncryptionOption.CustomPassword -> "Custom Password"
        is ToptyEncryptionOption.None -> "Unencrypted"
    }

val ToptyEncryptionOption.description: String
    get() = when (this) {
        is ToptyEncryptionOption.AppPassword -> "Encrypted with your app password. No extra steps needed."
        is ToptyEncryptionOption.CustomPassword -> "Encrypted with a new password specific to this backup."
        is ToptyEncryptionOption.None -> "No encryption. Anyone with this file can read your tokens."
    }
