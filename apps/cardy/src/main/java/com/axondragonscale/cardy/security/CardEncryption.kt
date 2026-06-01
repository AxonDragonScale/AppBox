/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.security

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

object CardEncryption {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12

    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = KeystoreManager.getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    fun decrypt(encrypted: ByteArray): ByteArray {
        require(encrypted.size > IV_LENGTH) { "Encrypted data too short" }
        val key = KeystoreManager.getOrCreateKey()
        val iv = encrypted.copyOfRange(0, IV_LENGTH)
        val ciphertext = encrypted.copyOfRange(IV_LENGTH, encrypted.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }

    fun encryptString(plaintext: String): ByteArray =
        encrypt(plaintext.toByteArray(Charsets.UTF_8))

    fun decryptString(encrypted: ByteArray): String =
        decrypt(encrypted).toString(Charsets.UTF_8)
}
