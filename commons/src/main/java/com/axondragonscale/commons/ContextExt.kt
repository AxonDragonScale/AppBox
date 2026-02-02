package com.axondragonscale.commons

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * Clears all app data and kills the process.
 * This will remove all preferences, databases, and cached files.
 *
 * Note: This action is irreversible and will terminate the app.
 */
fun Context.clearAppData() =
    getSystemService<ActivityManager>()?.clearApplicationUserData()
