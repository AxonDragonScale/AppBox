package com.axondragonscale.commons

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
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
    ContextCompat.startActivity(this, intent, null)
}