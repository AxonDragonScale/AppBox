package com.axondragonscale.limitless.ui.editor

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.axondragonscale.commons.canPostNotifications
import com.axondragonscale.commons.canScheduleExactAlarms
import com.axondragonscale.commons.openExactAlarmSettings

@Composable
actual fun rememberNotificationPermissionState(): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    var canPost by remember { mutableStateOf(context.canPostNotifications) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        canPost = granted
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canPost = context.canPostNotifications
    }

    val request: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    return canPost to request
}

@Composable
actual fun rememberExactAlarmState(): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    var canSchedule by remember { mutableStateOf(context.canScheduleExactAlarms) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canSchedule = context.canScheduleExactAlarms
    }

    val openSettings: () -> Unit = {
        context.openExactAlarmSettings()
    }

    return canSchedule to openSettings
}
