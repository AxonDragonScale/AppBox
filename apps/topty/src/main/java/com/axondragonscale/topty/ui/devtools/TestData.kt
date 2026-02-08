package com.axondragonscale.topty.ui.devtools

import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.model.HashAlgo
import com.axondragonscale.topty.model.Protocol

/**
 * Created by Ronak Harkhani on 28/12/24
 */

/**
 * Generates 10 diverse test tokens covering various combinations of:
 * - Protocols (TOTP, HOTP)
 * - Hash algorithms (SHA1, SHA256, SHA512)
 * - Periods (30s, 60s)
 * - Digits (6, 8)
 * - Groups (Work, Personal, Banking, etc.)
 * - Favorite status
 * - Different issuers and names
 */
fun getTestTokens(): List<Token> {
    return listOf(
        // 1. Standard Google TOTP (SHA1, 30s, 6 digits) - Most common
        Token.fromPlaintext(
            name = "john.doe@gmail.com",
            issuer = "Google",
            group = "Personal",
            note = "Primary email account",
            secret = "JBSWY3DPEHPK3PXP",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA1,
            period = 30,
            counter = 0,
            digits = 6,
            isFavorite = true
        ),

        // 2. GitHub TOTP (SHA1, 30s, 6 digits) - Work account
        Token.fromPlaintext(
            name = "johndoe",
            issuer = "GitHub",
            group = "Work",
            note = "Development account",
            secret = "HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA1,
            period = 30,
            counter = 0,
            digits = 6,
            isFavorite = true
        ),

        // 3. Microsoft TOTP (SHA1, 30s, 6 digits) - Work
        Token.fromPlaintext(
            name = "john.doe@company.com",
            issuer = "Microsoft",
            group = "Work",
            note = "Office 365 account",
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA1,
            period = 30,
            counter = 0,
            digits = 6,
            isFavorite = false
        ),

        // 4. AWS TOTP (SHA256, 30s, 6 digits) - More secure hash
        Token.fromPlaintext(
            name = "root-account",
            issuer = "Amazon Web Services",
            group = "Work",
            note = "Production AWS account",
            secret = "MFRGGZDFMZTWQ2LK",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA256,
            period = 30,
            counter = 0,
            digits = 6,
            isFavorite = true
        ),

        // 5. Dropbox TOTP (SHA512, 30s, 8 digits) - Most secure
        Token.fromPlaintext(
            name = "personal",
            issuer = "Dropbox",
            group = "Personal",
            note = "Cloud storage",
            secret = "MZXW6YTBOI======",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA512,
            period = 30,
            counter = 0,
            digits = 8,
            isFavorite = false
        ),

        // 6. Bank TOTP (SHA256, 60s, 6 digits) - Longer period
        Token.fromPlaintext(
            name = "12345678",
            issuer = "Chase Bank",
            group = "Banking",
            note = "Primary checking account",
            secret = "JBSWY3DPEHPK3PXP",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA256,
            period = 60,
            counter = 0,
            digits = 6,
            isFavorite = true
        ),

        // 7. Steam HOTP (SHA1, 6 digits) - Counter-based
        Token.fromPlaintext(
            name = "steamuser123",
            issuer = "Steam",
            group = "Gaming",
            note = "Gaming platform",
            secret = "HXDMVJECJJWSRB3H",
            protocol = Protocol.HOTP,
            hashAlgo = HashAlgo.SHA1,
            period = 30,
            counter = 0,
            digits = 6,
            isFavorite = false
        ),

        // 8. Twitter/X (SHA1, 30s, 6 digits) - No group
        Token.fromPlaintext(
            name = "@johndoe",
            issuer = "Twitter",
            group = "",
            note = "",
            secret = "GEZDGNBVGY3TQOJQ",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA1,
            period = 30,
            counter = 0,
            digits = 6,
            isFavorite = false
        ),

        // 9. Discord (SHA256, 30s, 8 digits) - Gaming/Social
        Token.fromPlaintext(
            name = "JohnDoe#1234",
            issuer = "Discord",
            group = "Gaming",
            note = "Community server admin",
            secret = "MFRGGZDFMZTWQ2LK",
            protocol = Protocol.TOTP,
            hashAlgo = HashAlgo.SHA256,
            period = 30,
            counter = 0,
            digits = 8,
            isFavorite = false
        ),

        // 10. Custom VPN (HOTP, SHA512, 8 digits) - Enterprise
        Token.fromPlaintext(
            name = "vpn-user-001",
            issuer = "Company VPN",
            group = "Work",
            note = "VPN access token",
            secret = "MZXW6YTBOI======",
            protocol = Protocol.HOTP,
            hashAlgo = HashAlgo.SHA512,
            period = 30,
            counter = 5,
            digits = 8,
            isFavorite = false
        )
    )
}
