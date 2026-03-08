package com.axondragonscale.commons

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Android actual for [AppInfo].
 *
 * Call [AppInfo.init] in Application.onCreate() before Koin setup to populate version info.
 * After init, versionName, versionCode, and packageName are available everywhere.
 */
actual object AppInfo {

    private var _versionName: String = "unknown"
    private var _versionCode: Int = 0
    private var _packageName: String = "unknown"

    actual val versionName: String get() = _versionName
    actual val versionCode: Int get() = _versionCode
    actual val packageName: String get() = _packageName

    fun init(context: Context) {
        _packageName = context.packageName
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            _versionName = packageInfo.versionName ?: "unknown"
            _versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (_: PackageManager.NameNotFoundException) {
            // Leave defaults
        }
    }
}
