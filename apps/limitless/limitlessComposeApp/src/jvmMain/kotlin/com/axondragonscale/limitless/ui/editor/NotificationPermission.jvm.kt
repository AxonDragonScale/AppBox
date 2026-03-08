package com.axondragonscale.limitless.ui.editor

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionState(): Pair<Boolean, () -> Unit> {
    // No-op — notification permission not implemented on Desktop yet
    return true to {}
}

@Composable
actual fun rememberExactAlarmState(): Pair<Boolean, () -> Unit> {
    // No-op — exact alarms not supported on Desktop
    return true to {}
}
