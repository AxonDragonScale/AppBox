package com.axondragonscale.devinator.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Created by Ronak on 08/11/26 */

/**
 * Local-network access is only needed for mDNS port discovery, and only from the platform version
 * that gates it. A refusal costs the port, never a toggle.
 */
@SuppressLint("InlinedApi")
object LocalNetworkAccess {

    val permission: String = Manifest.permission.ACCESS_LOCAL_NETWORK

    val isRequired: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN

    fun isGranted(context: Context): Boolean =
        !isRequired || context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
