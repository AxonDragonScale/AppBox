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
) {
    val command: String? get() = port?.let { "adb connect $host:$it" }
}

sealed interface WriteResult {
    data object Ok : WriteResult

    /** The write landed but the system trust dialog is up, or it was declined. */
    data object AwaitingConsent : WriteResult

    data object Failed : WriteResult
}

const val WRITE_FAILED_MESSAGE = "Couldn't change this setting."

/**
 * A failure that names the row it belongs to. A screen with four identical switches, or a list of
 * seven tweaks, cannot say only "a setting" and leave the user guessing which one to retry.
 */
fun writeFailedMessage(setting: String) = "Couldn't change $setting."
