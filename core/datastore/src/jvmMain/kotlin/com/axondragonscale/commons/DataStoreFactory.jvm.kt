package com.axondragonscale.commons

import java.io.File

actual fun dataStorePreferencesPath(name: String): String {
    val appDataDir = System.getProperty("user.home") + "/.local/share/jest"
    val dir = File(appDataDir)
    if (!dir.exists()) dir.mkdirs()
    return "$appDataDir/$name.preferences_pb"
}
