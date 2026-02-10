package com.axondragonscale.topty.security

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

/**
 * Helper object for token secret encryption/decryption.
 * Bridges the Token entity with EncryptionKeyManager and PasswordEncryption.
 */
object TokenEncryption: KoinComponent {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ALGORITHM = "AES"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SEPARATOR = "]" // Separator between IV and ciphertext

    private val passwordManager: PasswordManager by inject()

    fun encrypt(plainSecret: String) =
        encrypt(plainSecret, SecretKeySpec(passwordManager.getEncryptionKey(), ALGORITHM))

    fun encrypt(plainSecret: String, key: ByteArray) =
        encrypt(plainSecret, SecretKeySpec(key, ALGORITHM))

    fun encrypt(plainSecret: String, key: Key): String {
        if (plainSecret.isEmpty()) return ""

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plainSecret.toByteArray(Charsets.UTF_8))

        // Combine IV and ciphertext: "IV]CIPHERTEXT"
        val ivBase64 = Base64.encode(iv)
        val ciphertextBase64 = Base64.encode(ciphertext)

        return "$ivBase64$IV_SEPARATOR$ciphertextBase64"
    }

    fun decrypt(encryptedSecret: String) =
        decrypt(encryptedSecret, SecretKeySpec(passwordManager.getEncryptionKey(), ALGORITHM))

    fun decrypt(encryptedSecret: String, key: ByteArray) =
        decrypt(encryptedSecret, SecretKeySpec(key, ALGORITHM))

    fun decrypt(encryptedSecret: String, key: Key): String {
        if (encryptedSecret.isEmpty()) return ""

        // Split IV and ciphertext
        val parts = encryptedSecret.split(IV_SEPARATOR)
        if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted data format")

        val iv = Base64.decode(parts[0])
        val ciphertext = Base64.decode(parts[1])

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }
}
