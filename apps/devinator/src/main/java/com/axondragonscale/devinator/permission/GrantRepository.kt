package com.axondragonscale.devinator.permission

import android.content.Context

/** Created by Ronak on 08/11/26 */

/** The one place that answers whether the secure-settings grant is present. */
class GrantRepository(context: Context) {

    private val appContext = context.applicationContext

    val packageName: String get() = appContext.packageName

    fun isGranted(): Boolean = SecureSettingsPermission.isGranted(appContext)
}
