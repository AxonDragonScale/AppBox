package com.axondragonscale.qrqanvas.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ronak Harkhani on 09/07/25
 */
@Serializable
sealed interface QRContent {

    /**
     * Encodes this content into the raw string format that gets embedded in the QR code.
     */
    fun encode(): String

    @Serializable
    @SerialName("text")
    data class Text(val value: String) : QRContent {
        override fun encode(): String = value
    }

    @Serializable
    @SerialName("url")
    data class Url(val url: String) : QRContent {
        override fun encode(): String = url
    }

    @Serializable
    @SerialName("wifi")
    data class WiFi(
        val ssid: String,
        val password: String,
        val encryptionType: WifiEncryption = WifiEncryption.WPA,
        val hidden: Boolean = false,
    ) : QRContent {
        override fun encode(): String = buildString {
            append("WIFI:")
            append("T:${encryptionType.protocol};")
            append("S:${escapeSpecial(ssid)};")
            if (encryptionType != WifiEncryption.Open) {
                append("P:${escapeSpecial(password)};")
            }
            if (hidden) append("H:true;")
            append(";")
        }
    }

    @Serializable
    @SerialName("contact")
    data class Contact(
        val name: String,
        val phone: String? = null,
        val email: String? = null,
        val organization: String? = null,
        val title: String? = null,
        val url: String? = null,
        val address: String? = null,
        val note: String? = null,
    ) : QRContent {
        override fun encode(): String = buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("N:$name")
            appendLine("FN:$name")
            phone?.let { appendLine("TEL:$it") }
            email?.let { appendLine("EMAIL:$it") }
            organization?.let { appendLine("ORG:$it") }
            title?.let { appendLine("TITLE:$it") }
            url?.let { appendLine("URL:$it") }
            address?.let { appendLine("ADR:$it") }
            note?.let { appendLine("NOTE:$it") }
            append("END:VCARD")
        }
    }

    @Serializable
    @SerialName("email")
    data class Email(
        val address: String,
        val subject: String? = null,
        val body: String? = null,
    ) : QRContent {
        override fun encode(): String = buildString {
            append("mailto:$address")
            val params = mutableListOf<String>()
            subject?.let { params.add("subject=${it}") }
            body?.let { params.add("body=${it}") }
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
    }

    @Serializable
    @SerialName("phone")
    data class Phone(val number: String) : QRContent {
        override fun encode(): String = "tel:$number"
    }

    @Serializable
    @SerialName("sms")
    data class Sms(
        val number: String,
        val message: String? = null,
    ) : QRContent {
        override fun encode(): String = buildString {
            append("smsto:$number")
            message?.let { append(":$it") }
        }
    }

    @Serializable
    @SerialName("location")
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val label: String? = null,
    ) : QRContent {
        override fun encode(): String = buildString {
            append("geo:$latitude,$longitude")
            label?.let { append("?q=$it") }
        }
    }
}

/**
 * Escapes special characters in WiFi SSID/password values per the WiFi QR spec.
 * Characters \, ;, , and : must be escaped with a backslash.
 */
private fun escapeSpecial(value: String): String =
    value.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace(":", "\\:")
        .replace("\"", "\\\"")
