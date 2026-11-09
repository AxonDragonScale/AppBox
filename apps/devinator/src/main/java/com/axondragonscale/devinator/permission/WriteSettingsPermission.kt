package com.axondragonscale.devinator.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Created by Ronak on 09/11/26 */

/**
 * `WRITE_SETTINGS` is a special app access, separate from the secure-settings grant. The user can
 * grant it from the system's own toggle, so no adb step is needed; on Android 16+ `pm grant` is
 * refused anyway ("Permission ... is managed by role").
 */
object WriteSettingsPermission {

    fun isGranted(context: Context): Boolean = Settings.System.canWrite(context)

    /** Settings › Special app access › Modify system settings, scoped to this package. */
    fun settingsIntent(packageName: String): Intent = Intent(
        Settings.ACTION_MANAGE_WRITE_SETTINGS,
        Uri.parse("package:$packageName"),
    )
}
