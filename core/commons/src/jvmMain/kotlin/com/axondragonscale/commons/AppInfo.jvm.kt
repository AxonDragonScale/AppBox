package com.axondragonscale.commons

/**
 * Desktop (JVM) actual for [AppInfo].
 *
 * Call [AppInfo.init] before starting the app to populate version info.
 */
actual object AppInfo {

    private var _versionName: String = "unknown"
    private var _versionCode: Int = 0
    private var _packageName: String = "unknown"

    actual val versionName: String get() = _versionName
    actual val versionCode: Int get() = _versionCode
    actual val packageName: String get() = _packageName

    fun init(
        versionName: String,
        versionCode: Int,
        packageName: String,
    ) {
        _versionName = versionName
        _versionCode = versionCode
        _packageName = packageName
    }
}
