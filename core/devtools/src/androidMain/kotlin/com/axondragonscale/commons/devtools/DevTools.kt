package com.axondragonscale.commons.devtools

import android.content.Context
import com.axondragonscale.commons.devtools.logs.data.installDevLogStore

/**
 * Installs every dev tool that needs a host to start it. Call once, early; a new tool adds a line
 * here rather than another call in each app.
 */
fun installDevTools(context: Context) {
    installDevLogStore(context)
}
