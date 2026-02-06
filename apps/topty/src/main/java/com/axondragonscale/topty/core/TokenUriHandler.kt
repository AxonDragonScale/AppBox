package com.axondragonscale.topty.core

import android.net.Uri
import com.axondragonscale.topty.Scheme
import com.axondragonscale.topty.UriParam
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol
import com.axondragonscale.commons.logD
import androidx.core.net.toUri

/**
 * Created by Ronak Harkhani on 27/10/24
 *
 * Format Reference - https://github.com/google/google-authenticator/wiki/Key-Uri-Format#algorithm
 * Example - otpauth://totp/ACME%20Co:john.doe@email.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&algorithm=SHA1&digits=6&period=30
 */
object TokenUriHandler {

    private val supportedSchemes = listOf(Scheme.OTPAUTH)
    private val supportedProtocols = listOf(Protocol.TOTP.code, Protocol.HOTP.code)
    private val supportedAlgorithms = listOf(HashAlgo.SHA1.name, HashAlgo.SHA256.name, HashAlgo.SHA512.name)

    fun isValidTokenUri(uri: String?): Boolean {
        this.logD { "isValidTokenUri start" }
        if (uri == null) return false
        val uri = uri.toUri()

        // Verify scheme
        this.logD { "scheme = ${uri.scheme}" }
        if (uri.scheme !in supportedSchemes) return false

        // Verify Protocol
        this.logD { "host = ${uri.host}" }
        if (uri.host !in supportedProtocols) return false

        // Verify secret exists
        val encodedSecret = uri.getQueryParameter(UriParam.SECRET)
        this.logD { "secret = $encodedSecret" }
        if (encodedSecret.isNullOrEmpty()) return false

        return true
    }

    fun parseTokenUri(uri: String?): Token? {
        if (uri == null) return null
        val uri = uri.toUri()

        // Verify Scheme
        if (uri.scheme !in supportedSchemes) return null

        // Verify & set protocol
        val protocol = when (uri.host) {
            Protocol.TOTP.code -> Protocol.TOTP
            Protocol.HOTP.code -> Protocol.HOTP
            else -> return null
        }

        // Verify & set name (and issuer if it exists)
        val label = uri.path?.removePrefix("/") ?: ""
        this.logD { "label = $label" }
        var tokenName = ""
        var tokenIssuer = ""
        if (label.contains(":")) {
            val parts = label.split(":")
            tokenIssuer = parts.getOrElse(0) { "" }
            tokenName = parts.getOrElse(1) { "" }
        } else {
            tokenName = label
        }

        // Use explicit issuer if provided
        val issuer = uri.getQueryParameter(UriParam.ISSUER)
        if (!issuer.isNullOrEmpty()) {
            tokenIssuer = issuer
        }

        // Verify & set secret
        val encodedSecret = uri.getQueryParameter(UriParam.SECRET)
        this.logD { "secret = $encodedSecret" }
        if (encodedSecret.isNullOrEmpty()) return null

        // Verify & set algorithm
        val algorithm = uri.getQueryParameter(UriParam.ALGORITHM)
        this.logD { "algorithm = $algorithm" }
        val hashAlgo = if (!algorithm.isNullOrEmpty() && algorithm in supportedAlgorithms) {
            HashAlgo.valueOf(algorithm)
        } else {
            HashAlgo.SHA1
        }

        // Verify & set digits
        val digits = uri.getQueryParameter(UriParam.DIGITS)?.toIntOrNull()
        this.logD { "digits = $digits" }
        val tokenDigits = if (digits != null && digits in 6..8) digits else 6

        // Verify & set period
        val period = uri.getQueryParameter(UriParam.PERIOD)?.toIntOrNull()
        this.logD { "period = $period" }
        val tokenPeriod = if (period != null && period in 30..3600) period else 30

        // Verify & set counter (Mandatory for HOTP)
        val tokenCounter = if (protocol == Protocol.HOTP) {
            val counter = uri.getQueryParameter(UriParam.COUNTER)?.toLongOrNull()
            this.logD { "counter = $counter" }
            counter ?: return null
        } else {
            0L
        }

        // Create token with encrypted secret
        return Token.fromPlaintext(
            name = tokenName,
            issuer = tokenIssuer,
            group = "",
            note = "",
            secret = encodedSecret,
            protocol = protocol,
            hashAlgo = hashAlgo,
            period = tokenPeriod,
            counter = tokenCounter,
            digits = tokenDigits,
        )
    }
}
