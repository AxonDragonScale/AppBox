/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.security

/**
 * Tracks session-level state. Lives as a Koin singleton for the app process lifetime.
 * Biometric authentication is only required once per app session.
 */
class SessionManager {

    @Volatile
    var isAuthenticated: Boolean = false
        private set

    fun markAuthenticated() {
        isAuthenticated = true
    }
}
