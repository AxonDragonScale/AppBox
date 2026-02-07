package com.axondragonscale.topty.auth

import android.content.Context
import androidx.biometric.BiometricManager
import com.axondragonscale.commons.Result

/**
 * Utility class to check biometric authentication availability.
 * This class only requires Context and can be safely injected into ViewModels.
 *
 * Created by Ronak Harkhani on 25/01/25
 */
class BiometricAvailabilityChecker(
    private val context: Context
) {

    private val biometricManager = BiometricManager.from(context)

    /**
     * Check if biometric authentication is available on the device.
     * @return true if biometric authentication is available and can be used
     */
    fun isAvailable(): Boolean {
        return canAuthenticate().isSuccess()
    }

    /**
     * Get detailed status about biometric availability.
     * @return Result.Success if available, Result.Failure with error message if unavailable
     */
    fun canAuthenticate(): Result<Unit, String> {
        return when (val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Result.Success(Unit)

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Result.Failure("No biometric hardware available on this device")

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Result.Failure("Biometric hardware is currently unavailable")

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Result.Failure("No biometric credentials enrolled. Please set up fingerprint in device settings")

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                Result.Failure("Security update required")

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                Result.Failure("Biometric authentication is not supported")

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                Result.Failure("Biometric status unknown")

            else ->
                Result.Failure("Biometric authentication unavailable (code: $result)")
        }
    }
}
