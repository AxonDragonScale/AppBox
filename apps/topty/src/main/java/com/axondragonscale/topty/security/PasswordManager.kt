package com.axondragonscale.topty.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKeys
import com.axondragonscale.topty.model.PasswordStrength
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64
import kotlin.random.Random

/**
 * Manages password validation and strength checking.
 */
class PasswordManager(
    private val context: Context,
    private val prefsRepository: ToptyPrefsRepository,
) {

    companion object {
        private const val MIN_PASSWORD_LENGTH = 2
        private const val STRONG_PASSWORD_LENGTH = 12

        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16

        private const val ENCRYPTION_PREFS_FILE_NAME = "topty_encrypted_prefs"
        private const val ENCRYPTION_KEY_NAME = "encryption_key_name"
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            ENCRYPTION_PREFS_FILE_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            PrefKeyEncryptionScheme.AES256_SIV,
            PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Validates if a password meets minimum requirements.
     *
     * @return Error message if invalid, null if valid
     */
    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    /**
     * Checks the strength of a password.
     */
    fun checkPasswordStrength(password: String): PasswordStrength {
        if (password.length < MIN_PASSWORD_LENGTH) return PasswordStrength.WEAK

        var score = 0
        if (password.length >= MIN_PASSWORD_LENGTH) score++
        if (password.length >= STRONG_PASSWORD_LENGTH) score++

        // Character variety
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    suspend fun setPassword(password: String) {
        // Derive encryption key from password
        val keyDerivationSalt = generateSalt()
        val encryptionKey = deriveKey(password, keyDerivationSalt)

        // Save encryption key
        val encryptionKeyBase64 = Base64.encode(encryptionKey)
        encryptedPrefs.edit().putString(ENCRYPTION_KEY_NAME, encryptionKeyBase64).commit()

        // Hash password for verification
        val passwordHashSalt = generateSalt()
        val passwordHash = hashPassword(password, passwordHashSalt)

        // Store in preferences
        prefsRepository.passwordHash.set(passwordHash)
        prefsRepository.passwordHashSalt.set(Base64.encode(passwordHashSalt))
        prefsRepository.keyDerivationSalt.set(Base64.encode(keyDerivationSalt))
    }

    fun getEncryptionKey(): ByteArray {
        val encryptionKeyBase64 = encryptedPrefs.getString(ENCRYPTION_KEY_NAME, null)
            ?: throw IllegalStateException("Encryption key must always exist")
        return Base64.decode(encryptionKeyBase64)
    }

    /**
     * Verifies a password against stored hash.
     */
    suspend fun verifyPassword(password: String): Boolean {
        val passwordHashSalt = prefsRepository.passwordHashSalt.get()
        val passwordHash = hashPassword(password, Base64.decode(passwordHashSalt))
        return passwordHash == prefsRepository.passwordHash.get()
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val hash = deriveKey(password, salt)
        return Base64.encode(hash)
    }

    /**
     * Derives a 256-bit key from password and salt using PBKDF2.
     */
    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        require(salt.size == SALT_LENGTH_BYTES) { "Salt must be $SALT_LENGTH_BYTES bytes" }

        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private fun generateSalt() =
        ByteArray(SALT_LENGTH_BYTES).apply { Random.nextBytes(this) }

}
