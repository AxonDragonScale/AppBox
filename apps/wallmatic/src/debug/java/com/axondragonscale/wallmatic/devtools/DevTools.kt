package com.axondragonscale.wallmatic.devtools

import android.content.Context
// Aliased: this file's own `installDevTools` would otherwise shadow the module's.
import com.axondragonscale.commons.devtools.installDevTools as installCoreDevTools

/**
 * Created by Ronak Harkhani on 11/21/26
 *
 * Debug-only wiring. The release copy of this file is a no-op, so nothing here — and nothing in
 * `:core:devtools` — is reachable from, or compiled into, a release build.
 */

/** Installs the dev tools this build carries. Call before the first log you care about. */
fun installDevTools(context: Context) {
    installCoreDevTools(context)
}
