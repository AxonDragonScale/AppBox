package com.axondragonscale.limitless.ui.editor

import androidx.compose.runtime.Composable

/**
 * Created by Ronak Harkhani on 26/06/25
 *
 * Returns whether the app has notification permission and a lambda to request it.
 *
 * Android: checks POST_NOTIFICATIONS (API 33+) and uses ActivityResultContracts.
 * Desktop / iOS: always returns true (no-op — notifications not supported yet).
 */
@Composable
expect fun rememberNotificationPermissionState(): Pair<Boolean, () -> Unit>

/**
 * Returns whether the app can schedule exact alarms and a lambda to open the settings page.
 *
 * Android: checks SCHEDULE_EXACT_ALARM and opens the system settings screen.
 * Desktop / iOS: always returns true (no-op — exact alarms not supported yet).
 */
@Composable
expect fun rememberExactAlarmState(): Pair<Boolean, () -> Unit>
