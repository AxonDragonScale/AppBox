package com.axondragonscale.qrqanvas.ui.creator

import com.axondragonscale.qrqanvas.model.WifiEncryption

/**
 * Created by Ronak Harkhani on 13/02/26
 *
 * Holds the input field values for each content type in the Creator screen.
 * Each subtype corresponds to a [ContentType] and contains the fields
 * needed to construct the matching [com.axondragonscale.qrqanvas.model.QRContent] subtype.
 */
sealed interface ContentInputState {

    /** Returns true if the input has enough data to generate a valid QR code. */
    val isValid: Boolean

    data class Text(val value: String = "") : ContentInputState {
        override val isValid: Boolean get() = value.isNotBlank()
    }

    data class Url(val url: String = "") : ContentInputState {
        override val isValid: Boolean get() = url.isNotBlank()
    }

    data class WiFi(
        val ssid: String = "",
        val password: String = "",
        val encryption: WifiEncryption = WifiEncryption.WPA,
    ) : ContentInputState {
        override val isValid: Boolean get() = ssid.isNotBlank()
    }

    data class Email(
        val address: String = "",
        val subject: String = "",
        val body: String = "",
    ) : ContentInputState {
        override val isValid: Boolean get() = address.isNotBlank()
    }

    data class Phone(val number: String = "") : ContentInputState {
        override val isValid: Boolean get() = number.isNotBlank()
    }

    data class Sms(
        val number: String = "",
        val message: String = "",
    ) : ContentInputState {
        override val isValid: Boolean get() = number.isNotBlank()
    }

    data class Contact(
        val name: String = "",
        val phone: String = "",
        val email: String = "",
        val organization: String = "",
        val title: String = "",
        val url: String = "",
        val address: String = "",
        val note: String = "",
    ) : ContentInputState {
        override val isValid: Boolean get() = name.isNotBlank()
    }

    data class Location(
        val latitude: String = "",
        val longitude: String = "",
        val label: String = "",
    ) : ContentInputState {
        override val isValid: Boolean
            get() = latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null
    }
}
