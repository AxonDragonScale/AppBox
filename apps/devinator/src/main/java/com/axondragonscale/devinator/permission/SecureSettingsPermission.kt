package com.axondragonscale.devinator.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

/** Created by Ronak on 11/06/26 */

/**
 * The one permission the app cannot ask for at runtime: `WRITE_SECURE_SETTINGS` is only grantable
 * from a computer over adb, once, and it survives reboots and updates.
 */
object SecureSettingsPermission {

    private const val PERMISSION = Manifest.permission.WRITE_SECURE_SETTINGS

    fun isGranted(context: Context): Boolean =
        context.checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED

    /**
     * The command the user pastes on their computer. [packageName] must be the installed package:
     * debug builds carry a `.debug` suffix, so a hardcoded name would target a package that is not
     * on the device.
     */
    fun grantCommand(packageName: String): String =
        "adb shell pm grant $packageName $PERMISSION"

    /** The same command backwards, for anyone who wants the permission off the device. */
    fun revokeCommand(packageName: String): String =
        "adb shell pm revoke $packageName $PERMISSION"

    /** Step 1 of setup: the screen where USB debugging is switched on. */
    fun developmentSettingsIntent(): Intent =
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
}
