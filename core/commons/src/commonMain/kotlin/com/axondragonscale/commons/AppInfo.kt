package com.axondragonscale.commons

/**
 * Created by Ronak Harkhani on 07 March 2026.
 *
 * Provides app metadata (version name, version code, package name) across all platforms.
 *
 * Android: Call [AppInfo.init] in Application.onCreate() before Koin setup.
 * Desktop: Call [AppInfo.init] before starting the app.
 * iOS: Reads from NSBundle automatically — no init needed.
 */
expect object AppInfo {
    val versionName: String
    val versionCode: Int
    val packageName: String
}
