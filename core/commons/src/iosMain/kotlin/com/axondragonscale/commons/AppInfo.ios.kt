package com.axondragonscale.commons

import platform.Foundation.NSBundle

/**
 * iOS actual for [AppInfo].
 *
 * Reads version info directly from NSBundle.mainBundle — no init needed.
 */
actual object AppInfo {

    actual val versionName: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
            as? String ?: "unknown"

    actual val versionCode: Int
        get() = (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)
            ?.toIntOrNull() ?: 0

    actual val packageName: String
        get() = NSBundle.mainBundle.bundleIdentifier ?: "unknown"
}
