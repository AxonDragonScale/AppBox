package com.axondragonscale.topty.ui.onboarding

/**
 * Created by Ronak Harkhani on 28/12/24
 */
sealed interface OnboardingUiEffect {
    data object NavigateToHome : OnboardingUiEffect
    data object NavigateToQRScanner : OnboardingUiEffect
    data object NavigateToAddToken : OnboardingUiEffect
    data object NavigateToBackupRestore : OnboardingUiEffect
}
