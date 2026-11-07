package com.axondragonscale.devinator.model

/** Created by Ronak on 11/06/26 */

/**
 * The three switches. Every value is read live from `Settings.Global`; nothing here is persisted.
 */
data class DevState(
    val developerOptions: Boolean = false,
    val usbDebugging: Boolean = false,
    val wirelessDebugging: Boolean = false,
)

enum class EndpointKind { WIFI, HOTSPOT, VPN, OTHER }

/** One `adb connect` candidate. [port] is null while mDNS has not reported one. */
data class AdbEndpoint(
    val kind: EndpointKind,
    val host: String,
    val port: Int?,
)

sealed interface WriteResult {
    data object Ok : WriteResult

    /** The write landed but the system trust dialog is up, or it was declined. */
    data object AwaitingConsent : WriteResult

    data class Failed(val reason: String) : WriteResult
}
