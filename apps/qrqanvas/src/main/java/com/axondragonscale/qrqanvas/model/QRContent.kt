package com.axondragonscale.qrqanvas.model

import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Serializable
sealed interface QRContent {

    @Serializable
    data class Text(val value: String) : QRContent

    @Serializable
    data class Url(val url: String) : QRContent

    @Serializable
    data class WiFi(
        val ssid: String,
        val password: String,
        val encryptionType: WifiEncryption = WifiEncryption.WPA,
        val hidden: Boolean = false,
    ) : QRContent

    @Serializable
    data class Contact(
        val name: String,
        val phone: String? = null,
        val email: String? = null,
        val organization: String? = null,
        val title: String? = null,
        val url: String? = null,
        val address: String? = null,
        val note: String? = null,
    ) : QRContent

    @Serializable
    data class Email(
        val address: String,
        val subject: String? = null,
        val body: String? = null,
    ) : QRContent

    @Serializable
    data class Phone(val number: String) : QRContent

    @Serializable
    data class Sms(
        val number: String,
        val message: String? = null,
    ) : QRContent

    @Serializable
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val label: String? = null,
    ) : QRContent
}
