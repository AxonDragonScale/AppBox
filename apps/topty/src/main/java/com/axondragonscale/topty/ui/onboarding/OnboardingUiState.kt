package com.axondragonscale.topty.ui.onboarding

import com.axondragonscale.topty.model.PasswordStrength

/**
 * Created by Ronak Harkhani on 28/12/24
 */
data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
    val password: String = "",
    val confirmPassword: String = "",
    val passwordError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK,
)
