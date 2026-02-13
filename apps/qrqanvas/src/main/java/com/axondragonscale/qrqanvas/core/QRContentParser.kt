package com.axondragonscale.qrqanvas.core

import com.axondragonscale.qrqanvas.model.QRContent
import com.axondragonscale.qrqanvas.model.WifiEncryption

/**
 * Created by Ronak Harkhani on 09/07/25
 *
 * Parses raw scanned QR code strings into typed [QRContent] instances.
 *
 * Supports detection of:
 * - WiFi configurations (`WIFI:...;;`)
 * - URLs (`http://`, `https://`)
 * - Email addresses (`mailto:...`)
 * - Phone numbers (`tel:...`)
 * - SMS messages (`smsto:...` or `sms:...`)
 * - vCard contacts (`BEGIN:VCARD...END:VCARD`)
 * - Geographic locations (`geo:...`)
 * - Plain text (fallback)
 */
class QRContentParser {

    /**
     * Parses a raw QR code string into the appropriate [QRContent] subtype.
     *
     * @param rawValue The raw string decoded from the QR code.
     * @return A typed [QRContent] representing the scanned data.
     */
    fun parse(rawValue: String): QRContent {
        val trimmed = rawValue.trim()
        return when {
            trimmed.startsWith("WIFI:", ignoreCase = true) -> parseWifi(trimmed)
            trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) -> parseVCard(trimmed)
            trimmed.startsWith("mailto:", ignoreCase = true) -> parseEmail(trimmed)
            trimmed.startsWith("tel:", ignoreCase = true) -> parsePhone(trimmed)
            trimmed.startsWith("smsto:", ignoreCase = true) -> parseSms(trimmed, "smsto:")
            trimmed.startsWith("sms:", ignoreCase = true) -> parseSms(trimmed, "sms:")
            trimmed.startsWith("geo:", ignoreCase = true) -> parseGeo(trimmed)
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> QRContent.Url(url = trimmed)
            else -> QRContent.Text(value = trimmed)
        }
    }

    /**
     * Parses a WiFi QR string of the format:
     * `WIFI:T:<encryption>;S:<ssid>;P:<password>;H:<hidden>;;`
     */
    private fun parseWifi(raw: String): QRContent.WiFi {
        val body = raw.removePrefix("WIFI:").removeSuffix(";")
        val params = parseWifiParams(body)

        val encryptionType = when (params["T"]?.uppercase()) {
            "WPA", "WPA2", "WPA/WPA2" -> WifiEncryption.WPA
            "WEP" -> WifiEncryption.WEP
            "nopass", "" -> WifiEncryption.Open
            else -> WifiEncryption.WPA
        }

        return QRContent.WiFi(
            ssid = unescapeWifiValue(params["S"] ?: ""),
            password = unescapeWifiValue(params["P"] ?: ""),
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

    /**
     * Unescapes WiFi special characters: `\;` → `;`, `\,` → `,`, `\:` → `:`, `\\` → `\`.
     */
    private fun unescapeWifiValue(value: String): String =
        value.replace("\\;", ";")
            .replace("\\,", ",")
            .replace("\\:", ":")
            .replace("\\\\", "\\")
            .replace("\\\"", "\"")

    /**
     * Parses a vCard string into a [QRContent.Contact].
     */
    private fun parseVCard(raw: String): QRContent.Contact {
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
                upperLine.startsWith("TEL") -> phone = extractVCardValue(line)
                upperLine.startsWith("EMAIL") -> email = extractVCardValue(line)
                upperLine.startsWith("ORG:") -> organization = line.substringAfter(":")
                upperLine.startsWith("TITLE:") -> title = line.substringAfter(":")
                upperLine.startsWith("URL:") -> url = line.substringAfter(":")
                upperLine.startsWith("ADR") -> address = extractVCardValue(line)
                upperLine.startsWith("NOTE:") -> note = line.substringAfter(":")
            }
        }

        return QRContent.Contact(
            name = name,
            phone = phone,
            email = email,
            organization = organization,
            title = title,
            url = url,
            address = address,
            note = note,
        )
    }

    /**
     * Extracts the value part of a vCard line that may contain type parameters,
     * e.g. `TEL;TYPE=CELL:+1234567890` → `+1234567890`.
     */
    private fun extractVCardValue(line: String): String {
        val colonIndex = line.indexOf(':')
        return if (colonIndex >= 0) line.substring(colonIndex + 1) else line
    }

    /**
     * Parses a `mailto:` URI into a [QRContent.Email].
     * Supports optional `subject` and `body` query parameters.
     */
    private fun parseEmail(raw: String): QRContent.Email {
        val afterScheme = raw.removePrefix("mailto:")
        val questionIndex = afterScheme.indexOf('?')
        val address: String
        val queryParams: Map<String, String>

        if (questionIndex >= 0) {
            address = afterScheme.substring(0, questionIndex)
            queryParams = parseQueryParams(afterScheme.substring(questionIndex + 1))
        } else {
            address = afterScheme
            queryParams = emptyMap()
        }

        return QRContent.Email(
            address = address,
            subject = queryParams["subject"],
            body = queryParams["body"],
        )
    }

    /**
     * Parses a `tel:` URI into a [QRContent.Phone].
     */
    private fun parsePhone(raw: String): QRContent.Phone {
        return QRContent.Phone(number = raw.removePrefix("tel:"))
    }

    /**
     * Parses an `smsto:` or `sms:` URI into a [QRContent.Sms].
     * Format: `smsto:<number>:<message>` or `smsto:<number>?body=<message>`
     */
    private fun parseSms(raw: String, scheme: String): QRContent.Sms {
        val afterScheme = raw.removePrefix(scheme)

        // Check for query-style body: smsto:+123?body=Hello
        val questionIndex = afterScheme.indexOf('?')
        if (questionIndex >= 0) {
            val number = afterScheme.substring(0, questionIndex)
            val params = parseQueryParams(afterScheme.substring(questionIndex + 1))
            return QRContent.Sms(
                number = number,
                message = params["body"],
            )
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

    /**
     * Parses a `geo:` URI into a [QRContent.Location].
     * Format: `geo:<lat>,<lng>` or `geo:<lat>,<lng>?q=<label>`
     */
    private fun parseGeo(raw: String): QRContent {
        val afterScheme = raw.removePrefix("geo:")
        val questionIndex = afterScheme.indexOf('?')
        val coordsPart: String
        val queryParams: Map<String, String>

        if (questionIndex >= 0) {
            coordsPart = afterScheme.substring(0, questionIndex)
            queryParams = parseQueryParams(afterScheme.substring(questionIndex + 1))
        } else {
            coordsPart = afterScheme
            queryParams = emptyMap()
        }

        val parts = coordsPart.split(",")
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lng = parts.getOrNull(1)?.toDoubleOrNull()

        return if (lat != null && lng != null) {
            QRContent.Location(
                latitude = lat,
                longitude = lng,
                label = queryParams["q"],
            )
        } else {
            // Couldn't parse coordinates — fallback to text
            QRContent.Text(value = raw)
        }
    }

    /**
     * Parses URL-style query parameters (`key=value&key2=value2`) into a map.
     */
    private fun parseQueryParams(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split("&").mapNotNull { param ->
            val eqIndex = param.indexOf('=')
            if (eqIndex > 0) {
                val key = param.substring(0, eqIndex).lowercase()
                val value = param.substring(eqIndex + 1)
                key to value
            } else null
        }.toMap()
    }
}
