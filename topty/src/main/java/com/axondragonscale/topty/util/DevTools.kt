package com.axondragonscale.topty.util

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService

/**
 * Created by Ronak Harkhani on 26/09/24
 */
class DevTools(private val context: Context) {
    fun clearData() {
        context.getSystemService<ActivityManager>()?.clearApplicationUserData()
    }
}
