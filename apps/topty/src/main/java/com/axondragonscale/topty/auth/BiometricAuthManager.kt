package com.axondragonscale.topty.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.axondragonscale.commons.Result

/**
 * Created by Ronak Harkhani on 25/01/25
 */
class BiometricAuthManager(
    private val activity: FragmentActivity,
) {

    private val biometricManager = BiometricManager.from(activity)

    fun canAuthenticate(): Result<Unit, String> {
        return when (val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Result.Success(Unit)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Result.Failure("No biometric hardware available")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Result.Failure("Biometric hardware is currently unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Result.Failure("No biometric credentials enrolled")
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Result.Failure("Security update required")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> Result.Failure("Biometric authentication is not supported")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> Result.Failure("Biometric status unknown")
            else -> Result.Failure("Biometric authentication unavailable (code: $result)")
        }
    }

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Topty")
            .setSubtitle("Use your fingerprint to access your tokens")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Only report actual errors, not user cancellation
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
