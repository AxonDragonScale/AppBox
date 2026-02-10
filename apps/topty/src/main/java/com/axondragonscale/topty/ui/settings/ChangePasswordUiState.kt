package com.axondragonscale.topty.ui.settings

import com.axondragonscale.topty.model.PasswordStrength

/**
 * Created by Ronak Harkhani on 29/01/25
 */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK,
    val isProcessing: Boolean = false,
    val reEncryptionProgress: Pair<Int, Int>? = null,
    val successMessage: String? = null,
)
