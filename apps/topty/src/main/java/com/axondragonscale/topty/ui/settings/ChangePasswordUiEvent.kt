package com.axondragonscale.topty.ui.settings

/**
 * Created by Ronak Harkhani on 29/01/25
 */
sealed interface ChangePasswordUiEvent {
    data object NavigateBack : ChangePasswordUiEvent
    data class CurrentPasswordChanged(val password: String) : ChangePasswordUiEvent
    data class NewPasswordChanged(val password: String) : ChangePasswordUiEvent
    data class ConfirmPasswordChanged(val password: String) : ChangePasswordUiEvent
    data object ChangePassword : ChangePasswordUiEvent
}
