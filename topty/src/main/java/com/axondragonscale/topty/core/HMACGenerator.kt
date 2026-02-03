package com.axondragonscale.topty.core

import com.axondragonscale.topty.model.HashAlgo
import com.google.common.io.BaseEncoding
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow

/**
 * Created by Ronak Harkhani on 28/10/24
 *
 * Spec - https://datatracker.ietf.org/doc/html/rfc4226
 */
object HMACGenerator {

    fun generateCode(secret: String, counter: Long, hashAlgo: HashAlgo, digits: Int): String {
        // Convert the counter to a byte array (Big Endian by default)
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()

        // Calculate the HMAC hash based on secret and counterBytes
        val key = BaseEncoding.base32().decode(secret)
        val mac = Mac.getInstance(hashAlgo.hmacAlgo)
        mac.init(SecretKeySpec(key, "RAW"))
        val hash = mac.doFinal(counterBytes)

        // Truncate the hash
        val offset = hash.last().and(0x0F).toInt()  // Offset is the last 4 bits of the hash
        val truncatedHash = ByteArray(4) { hash[offset + it] } // Take the next 4 bytes at offset
        truncatedHash[0] = truncatedHash[0].and(0x7F) // Clear the first bit of the first byte (sign bit)

        // Get code from hash
        val fullCode = ByteBuffer.wrap(truncatedHash).getInt() // Use 4 bytes (32 bits) as an int
        val code = fullCode % 10.0.pow(digits).toInt() // Get the last `digits` digits

        // Pad the code with leading zeros if necessary
        return code.toString().padStart(digits, '0')
    }

    private val HashAlgo.hmacAlgo: String
        get() = when (this) {
            HashAlgo.SHA1 -> "HmacSHA1"
            HashAlgo.SHA256 -> "HmacSHA256"
            HashAlgo.SHA512 -> "HmacSHA512"
        }

}
