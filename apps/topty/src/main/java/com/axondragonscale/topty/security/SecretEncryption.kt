package com.axondragonscale.topty.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Created by Ronak Harkhani on 27/01/25
 *
 * Utility for encrypting and decrypting token secrets using Android Keystore.
 * Uses AES-256 with GCM mode for authenticated encryption.
 */
object SecretEncryption {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "topty_master_key"

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SEPARATOR = "]" // Separator between IV and ciphertext

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    /**
     * Encrypts a plaintext secret.
     *
     * @param plaintext The secret to encrypt (Base32 encoded TOTP secret)
     * @return Base64 encoded string containing IV and ciphertext separated by "]"
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine IV and ciphertext: "IV]CIPHERTEXT"
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val ciphertextBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

        return "$ivBase64$IV_SEPARATOR$ciphertextBase64"
    }

    /**
     * Decrypts an encrypted secret.
     *
     * @param encrypted Base64 encoded string containing IV and ciphertext
     * @return The decrypted plaintext secret
     */
    fun decrypt(encrypted: String): String {
        if (encrypted.isEmpty()) return ""

        // Split IV and ciphertext
        val parts = encrypted.split(IV_SEPARATOR)
        if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted data format")

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    /**
     * Gets the encryption key from Keystore or creates a new one if it doesn't exist.
     */
    private fun getOrCreateKey(): SecretKey {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        return generateKey()
    }

    /**
     * Generates a new AES-256 key in Android Keystore.
     */
    private fun generateKey(): SecretKey {
        this.logD { "Generating new encryption key" }
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Don't require auth for every decrypt
            .setRandomizedEncryptionRequired(true) // Use random IV for each encryption
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

}
