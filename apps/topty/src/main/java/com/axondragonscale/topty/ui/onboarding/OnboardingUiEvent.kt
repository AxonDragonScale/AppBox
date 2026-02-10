package com.axondragonscale.topty.ui.onboarding

/**
 * Created by Ronak Harkhani on 28/12/24
 */
sealed interface OnboardingUiEvent {
    data object NextPage : OnboardingUiEvent
    data class PasswordChanged(val password: String) : OnboardingUiEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : OnboardingUiEvent
    data object SetPassword : OnboardingUiEvent
}
