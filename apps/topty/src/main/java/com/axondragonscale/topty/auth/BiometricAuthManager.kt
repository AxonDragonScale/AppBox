package com.axondragonscale.topty.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Created by Ronak Harkhani on 25/01/25
 *
 * Manages biometric authentication prompts.
 * Requires FragmentActivity to display authentication prompts.
 * For checking biometric availability without FragmentActivity, use BiometricAvailabilityChecker.
 */
class BiometricAuthManager(
    private val activity: FragmentActivity,
) {

    private val availabilityChecker = BiometricAvailabilityChecker(activity)

    fun canAuthenticate() = availabilityChecker.canAuthenticate()

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
