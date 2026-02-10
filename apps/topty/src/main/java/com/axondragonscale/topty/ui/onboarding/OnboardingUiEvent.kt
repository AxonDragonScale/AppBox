package com.axondragonscale.topty.ui.onboarding

/**
 * Created by Ronak Harkhani on 28/12/24
 */
sealed interface OnboardingUiEvent {
    data object NextPage : OnboardingUiEvent
    data object PreviousPage : OnboardingUiEvent
    data object SkipOnboarding : OnboardingUiEvent
    data object NavigateToQRScanner : OnboardingUiEvent
    data object NavigateToManualEntry : OnboardingUiEvent
    data object NavigateToImport : OnboardingUiEvent
    data class PasswordChanged(val password: String) : OnboardingUiEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : OnboardingUiEvent
    data object SetPassword : OnboardingUiEvent
}
