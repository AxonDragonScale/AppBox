package com.axondragonscale.qrqanvas.core

import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.WifiEncryption
import com.axondragonscale.qrqanvas.util.WIFI_ESCAPE_CHARS
import com.axondragonscale.qrqanvas.util.escapeBackslash
import com.axondragonscale.qrqanvas.util.unescapeBackslash

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * Encodes [QRContent] instances into raw QR code strings for embedding,
 * and parses raw scanned QR code strings back into typed [QRContent] instances.
 *
 * Supports:
 * - WiFi configurations (`WIFI:...;;`)
 * - URLs (`http://`, `https://`)
 * - Email addresses (`mailto:...`)
 * - Phone numbers (`tel:...`)
 * - SMS messages (`smsto:...` or `sms:...`)
 * - vCard contacts (`BEGIN:VCARD...END:VCARD`)
 * - Geographic locations (`geo:...`)
 * - Plain text (fallback)
 */
object QRContentCodec {

    /**
     * Encodes the given [content] into the raw string format that gets embedded in the QR code.
     */
    fun encode(content: QRContent): String = when (content) {
        is QRContent.Text -> encodeText(content)
        is QRContent.Url -> encodeUrl(content)
        is QRContent.WiFi -> encodeWifi(content)
        is QRContent.Contact -> encodeContact(content)
        is QRContent.Email -> encodeEmail(content)
        is QRContent.Phone -> encodePhone(content)
        is QRContent.Sms -> encodeSms(content)
        is QRContent.Location -> encodeLocation(content)
    }

    /**
     * Parses a raw QR code string into the appropriate [QRContent] subtype.
     *
     * @param rawValue The raw string decoded from the QR code.
     * @return A typed [QRContent] representing the scanned data.
     */
    fun decode(rawValue: String): QRContent {
        val trimmed = rawValue.trim()
        return when {
            trimmed.startsWith("WIFI:", ignoreCase = true) -> decodeWifi(trimmed)
            trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) -> decodeContact(trimmed)
            trimmed.startsWith("mailto:", ignoreCase = true) -> decodeEmail(trimmed)
            trimmed.startsWith("tel:", ignoreCase = true) -> decodePhone(trimmed)
            trimmed.startsWith("smsto:", ignoreCase = true) -> decodeSms(trimmed, "smsto:")
            trimmed.startsWith("sms:", ignoreCase = true) -> decodeSms(trimmed, "sms:")
            trimmed.startsWith("geo:", ignoreCase = true) -> decodeLocation(trimmed)
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> decodeUrl(trimmed)
            else -> decodeText(trimmed)
        }
    }

    // region Text

    private fun encodeText(content: QRContent.Text): String = content.value

    private fun decodeText(rawValue: String): QRContent.Text = QRContent.Text(value = rawValue)

    // endregion Text

    // region Url

    private fun encodeUrl(content: QRContent.Url): String = content.url

    private fun decodeUrl(rawValue: String): QRContent.Url = QRContent.Url(url = rawValue)

    // endregion Url

    // region WiFi

    private fun encodeWifi(content: QRContent.WiFi): String = buildString {
        append("WIFI:")
        append("T:${content.encryptionType.protocol};")
        append("S:${content.ssid.escapeBackslash(WIFI_ESCAPE_CHARS)};")
        if (content.encryptionType != WifiEncryption.Open) {
            append("P:${content.password.escapeBackslash(WIFI_ESCAPE_CHARS)};")
        }
        if (content.hidden) append("H:true;")
        append(";")
    }

    /**
     * Parses a WiFi QR string of the format:
     * `WIFI:T:<encryption>;S:<ssid>;P:<password>;H:<hidden>;;`
     */
    private fun decodeWifi(raw: String): QRContent.WiFi {
        val body = raw.removePrefix("WIFI:").removePrefix("wifi:").removeSuffix(";")
        val params = parseWifiParams(body)

        val encryptionType = when (params["T"]?.uppercase()) {
            "WPA", "WPA2", "WPA/WPA2" -> WifiEncryption.WPA
            "WEP" -> WifiEncryption.WEP
            "NOPASS", "" -> WifiEncryption.Open
            else -> WifiEncryption.WPA
        }

        return QRContent.WiFi(
            ssid = params["S"].orEmpty().unescapeBackslash(WIFI_ESCAPE_CHARS),
            password = params["P"].orEmpty().unescapeBackslash(WIFI_ESCAPE_CHARS),
            encryptionType = encryptionType,
            hidden = params["H"]?.equals("true", ignoreCase = true) == true,
        )
    }

    /**
     * Parses semicolon-separated `KEY:VALUE` pairs from a WiFi QR body,
     * respecting escaped semicolons (`\;`).
     */
    private fun parseWifiParams(body: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val current = StringBuilder()
        var i = 0
        while (i < body.length) {
            val ch = body[i]
            if (ch == '\\' && i + 1 < body.length) {
                // Escaped character — keep the next char literally
                current.append(body[i + 1])
                i += 2
            } else if (ch == ';') {
                val segment = current.toString()
                val colonIndex = segment.indexOf(':')
                if (colonIndex > 0) {
                    val key = segment.substring(0, colonIndex)
                    val value = segment.substring(colonIndex + 1)
                    params[key] = value
                }
                current.clear()
                i++
            } else {
                current.append(ch)
                i++
            }
        }
        // Handle last segment if no trailing semicolon
        if (current.isNotEmpty()) {
            val segment = current.toString()
            val colonIndex = segment.indexOf(':')
            if (colonIndex > 0) {
                val key = segment.substring(0, colonIndex)
                val value = segment.substring(colonIndex + 1)
                params[key] = value
            }
        }
        return params
    }

    // endregion

    // region vCard / Contact

    private fun encodeContact(content: QRContent.Contact): String = buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")
        appendLine("N:${content.name}")
        appendLine("FN:${content.name}")
        content.phone?.let { appendLine("TEL:$it") }
        content.email?.let { appendLine("EMAIL:$it") }
        content.organization?.let { appendLine("ORG:$it") }
        content.title?.let { appendLine("TITLE:$it") }
        content.url?.let { appendLine("URL:$it") }
        content.address?.let { appendLine("ADR:$it") }
        content.note?.let { appendLine("NOTE:$it") }
        append("END:VCARD")
    }

    /**
     * Parses a vCard string into a [QRContent.Contact].
     */
    private fun decodeContact(raw: String): QRContent.Contact {
        val lines = raw.lines().map { it.trim() }
        var name = ""
        var phone: String? = null
        var email: String? = null
        var organization: String? = null
        var title: String? = null
        var url: String? = null
        var address: String? = null
        var note: String? = null

        for (line in lines) {
            val upperLine = line.uppercase()
            when {
                upperLine.startsWith("FN:") -> name = line.substringAfter(":")
                upperLine.startsWith("N:") && name.isEmpty() -> name = line.substringAfter(":")
                upperLine.startsWith("TEL") -> phone = line.substringAfter(":")
                upperLine.startsWith("EMAIL") -> email = line.substringAfter(":")
                upperLine.startsWith("ORG:") -> organization = line.substringAfter(":")
                upperLine.startsWith("TITLE:") -> title = line.substringAfter(":")
                upperLine.startsWith("URL:") -> url = line.substringAfter(":")
                upperLine.startsWith("ADR") -> address = line.substringAfter(":")
                upperLine.startsWith("NOTE:") -> note = line.substringAfter(":")
            }
        }

        return QRContent.Contact(name, phone, email, organization, title, url, address, note)
    }

    // endregion

    // region Email

    private fun encodeEmail(content: QRContent.Email): String = buildString {
        append("mailto:${content.address}")
        val params = mutableListOf<String>()
        content.subject?.let { params.add("subject=$it") }
        content.body?.let { params.add("body=$it") }
        if (params.isNotEmpty()) append("?${params.joinToString("&")}")
    }

    /**
     * Parses a `mailto:` URI into a [QRContent.Email].
     * Supports optional `subject` and `body` query parameters.
     */
    private fun decodeEmail(raw: String): QRContent.Email {
        val afterScheme = raw.removePrefix("mailto:")
        val address = afterScheme.substringBefore('?')
        val queryParams = parseQueryParams(afterScheme)

        return QRContent.Email(address, queryParams["subject"], queryParams["body"])
    }

    // endregion

    // region Phone

    private fun encodePhone(content: QRContent.Phone): String = "tel:${content.number}"

    /**
     * Parses a `tel:` URI into a [QRContent.Phone].
     */
    private fun decodePhone(raw: String): QRContent.Phone {
        return QRContent.Phone(number = raw.removePrefix("tel:"))
    }

    // endregion

    // region SMS

    private fun encodeSms(content: QRContent.Sms): String = buildString {
        append("smsto:${content.number}")
        content.message?.let { append(":$it") }
    }

    /**
     * Parses an `smsto:` or `sms:` URI into a [QRContent.Sms].
     * Format: `smsto:<number>:<message>` or `smsto:<number>?body=<message>`
     */
    private fun decodeSms(raw: String, scheme: String): QRContent.Sms {
        val afterScheme = raw.removePrefix(scheme)

        // Check for query-style body: smsto:+123?body=Hello
        val questionIndex = afterScheme.indexOf('?')
        if (questionIndex >= 0) {
            val number = afterScheme.substring(0, questionIndex)
            val params = parseQueryParams(afterScheme.substring(questionIndex + 1))
            return QRContent.Sms(number = number, message = params["body"])
        }

        // Check for colon-separated message: smsto:+123:Hello
        val colonIndex = afterScheme.indexOf(':')
        return if (colonIndex >= 0) {
            QRContent.Sms(
                number = afterScheme.substring(0, colonIndex),
                message = afterScheme.substring(colonIndex + 1).ifEmpty { null },
            )
        } else {
            QRContent.Sms(number = afterScheme, message = null)
        }
    }

    // endregion

    // region Location

    private fun encodeLocation(content: QRContent.Location): String = buildString {
        append("geo:${content.latitude},${content.longitude}")
        content.label?.let { append("?q=$it") }
    }

    /**
     * Parses a `geo:` URI into a [QRContent.Location].
     * Format: `geo:<lat>,<lng>` or `geo:<lat>,<lng>?q=<label>`
     */
    private fun decodeLocation(raw: String): QRContent {
        val afterScheme = raw.removePrefix("geo:")

        val queryParams = parseQueryParams(afterScheme)
        val parts = afterScheme.substringBefore('?').split(",")
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lng = parts.getOrNull(1)?.toDoubleOrNull()

        return if (lat != null && lng != null) {
            QRContent.Location(latitude = lat, longitude = lng, label = queryParams["q"])
        } else {
            QRContent.Text(value = raw) // Couldn't parse coordinates — fallback to text
        }
    }

    // endregion

    // region Helpers

    /**
     * Parses URL-style query parameters (`xxx?key=value&key2=value2`) into a map.
     */
    private fun parseQueryParams(rawValue: String): Map<String, String> {
        val query = rawValue.substringAfter('?')
        if (query.isBlank() || !query.contains('=')) return emptyMap()
        return query.split("&").associate { param ->
            param.split('=').let {
                it.getOrNull(0).orEmpty() to it.getOrNull(1).orEmpty()
            }
        }
    }

    // endregion
}
