package com.axondragonscale.wallmatic.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Created by Ronak Harkhani on 05/07/24
 */

fun Context.takePersistableUriPermission(uri: Uri) {
    // Wallmatic only reads these URIs, and a picker may grant read without write.
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    this.contentResolver.takePersistableUriPermission(uri, flags)
}
