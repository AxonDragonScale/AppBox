package com.axondragonscale.wallmatic.util

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * Created by Ronak Harkhani on 26/09/24
 */
class DevTools(
    val context: Context,
) {
    fun clearData() {
        context.getSystemService<ActivityManager>()?.clearApplicationUserData()
    }
}
