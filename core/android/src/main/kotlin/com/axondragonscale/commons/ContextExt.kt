package com.axondragonscale.commons

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService

/**
 * Clears all app data and kills the process.
 * This will remove all preferences, databases, and cached files.
 *
 * Note: This action is irreversible and will terminate the app.
 */
fun Context.clearAppData() =
    getSystemService<ActivityManager>()?.clearApplicationUserData()

val Context.notificationManager: NotificationManager?
    get() = getSystemService<NotificationManager>()

fun Context.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    startActivity(intent)
}

val Context.canScheduleExactAlarms: Boolean
    get() = getSystemService<AlarmManager>()?.canScheduleExactAlarms() ?: false

val Context.canPostNotifications: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        true
    }

fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

fun Context.openExactAlarmSettings() {
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}
