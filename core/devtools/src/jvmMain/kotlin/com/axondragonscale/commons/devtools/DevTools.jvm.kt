package com.axondragonscale.commons.devtools

/**
 * Created by Ronak Harkhani on 11/21/26
 *
 * Installs every dev tool that needs a host to start it. Call once, early; a new tool adds a line
 * here rather than another call in each app.
 */
fun installDevTools() {
    installDevLogStore()
}
