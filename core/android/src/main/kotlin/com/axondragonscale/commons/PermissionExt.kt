package com.axondragonscale.commons

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.pm.PermissionInfo
import android.os.Build
import androidx.core.content.ContextCompat

data class PermissionState(
    val name: String,
    val granted: Boolean,
)

/**
 * Returns a list of dangerous (runtime) permissions declared in the app's manifest
 * along with their current grant status.
 */
fun Context.getDangerousPermissions(): List<PermissionState> {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
    } else {
        packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
    }

    val declaredPermissions = packageInfo.requestedPermissions ?: return emptyList()

    return declaredPermissions.mapNotNull { permission ->
        try {
            val permissionInfo = packageManager.getPermissionInfo(permission, 0)
            val isDangerous = permissionInfo.protection == PermissionInfo.PROTECTION_DANGEROUS

            if (isDangerous) {
                PermissionState(permission, this.isPermissionGranted(permission))
            } else {
                null
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}

/**
 * Checks if a specific permission is granted.
 */
fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * Returns a list of permission names that are not yet granted.
 */
fun List<PermissionState>.getMissingPermissions(): Array<String> =
    filter { !it.granted }.map { it.name }.toTypedArray()

/**
 * Marks all currently granted runtime permissions to be revoked when the process is next killed.
 * Has no effect below API 31.
 */
fun Context.revokeGrantedPermissionsOnKill(permissions: List<PermissionState>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = permissions.filter { it.granted }.map { it.name }.toSet()
        if (granted.isNotEmpty()) revokeSelfPermissionsOnKill(granted)
    }
}
