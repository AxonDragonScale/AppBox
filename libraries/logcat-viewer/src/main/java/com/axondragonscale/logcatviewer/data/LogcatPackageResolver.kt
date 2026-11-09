package com.axondragonscale.logcatviewer.data

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import java.io.File

/** Created by Ronak on 09/11/26 */

/**
 * Maps a package to the PIDs writing its lines. `getRunningAppProcesses` is best effort on modern
 * Android — it always includes our own process — so a package the platform hides resolves to no
 * PIDs and the stream honestly shows no matches for it.
 */
internal class LogcatPackageResolver(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val activityManager = appContext.getSystemService(ActivityManager::class.java)
    private val packageManager = appContext.packageManager

    fun pidsFor(packages: Set<String>): Set<Int> {
        if (packages.isEmpty()) return emptySet()
        return runCatching {
            val uids = packages.mapNotNull { pkg ->
                runCatching { packageManager.getPackageUid(pkg, 0) }.getOrNull()
            }.toSet()
            val pids = activityManager?.runningAppProcesses.orEmpty()
                .filter { it.uid in uids }
                .mapTo(mutableSetOf()) { it.pid }
            if (appContext.packageName in packages) pids += Process.myPid()
            pids.toSet()
        }.getOrDefault(emptySet())
    }

    fun packageForPid(pid: Int): String? {
        if (pid == Process.myPid()) return appContext.packageName
        val fromProcesses = runCatching {
            activityManager?.runningAppProcesses
                ?.firstOrNull { it.pid == pid }
                ?.processName
                ?.substringBefore(':')
        }.getOrNull()
        if (!fromProcesses.isNullOrBlank()) return fromProcesses
        // Process visibility hides other apps from ActivityManager; cmdline still often answers.
        return runCatching {
            File("/proc/$pid/cmdline")
                .readText()
                .trim()
                .substringBefore(':')
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
