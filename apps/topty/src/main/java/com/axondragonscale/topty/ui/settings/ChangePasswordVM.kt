package com.axondragonscale.topty.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.commons.logD
import com.axondragonscale.commons.logE
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.security.PasswordManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 29/01/25
 */
class ChangePasswordVM(
    private val tokenRepository: TokenRepository,
    private val passwordManager: PasswordManager,
) : ViewModel() {

    companion object {
        private const val TAG = "ChangePasswordVM"
    }

    val uiState = MutableStateFlow(ChangePasswordUiState())

    fun onEvent(event: ChangePasswordUiEvent): Any = when (event) {
        is ChangePasswordUiEvent.CurrentPasswordChanged -> onCurrentPasswordChanged(event.password)
        is ChangePasswordUiEvent.NewPasswordChanged -> onNewPasswordChanged(event.password)
        is ChangePasswordUiEvent.ConfirmPasswordChanged -> onConfirmPasswordChanged(event.password)
        is ChangePasswordUiEvent.ChangePassword -> onChangePassword()

        // Handled in composable
        is ChangePasswordUiEvent.NavigateBack -> Unit
    }

    private fun onCurrentPasswordChanged(password: String) {
        uiState.update { state ->
            state.copy(
                currentPassword = password,
                currentPasswordError = null,
            )
        }
    }

    private fun onNewPasswordChanged(password: String) {
        uiState.update { state ->
            val strength = passwordManager.checkPasswordStrength(password)
            val error = if (password.isEmpty()) null else passwordManager.validatePassword(password)
            state.copy(newPassword = password, passwordStrength = strength, newPasswordError = error)
        }
    }

    private fun onConfirmPasswordChanged(password: String) {
        uiState.update { state ->
            state.copy(confirmPassword = password)
        }
    }

    private fun onChangePassword() = viewModelScope.launch(Dispatchers.IO) {
        val state = uiState.value
        uiState.update { it.copy(isProcessing = true, successMessage = null) }

        try {
            val isCurrentPasswordCorrect = passwordManager.verifyPassword(state.currentPassword)
            if (!isCurrentPasswordCorrect) {
                uiState.update { it.copy(isProcessing = false, currentPasswordError = "Incorrect password") }
                return@launch
            }

            // Validate new password
            val newPasswordError = passwordManager.validatePassword(state.newPassword)
            if (newPasswordError != null) {
                uiState.update { it.copy(isProcessing = false, newPasswordError = newPasswordError) }
                return@launch
            }

            // Check passwords match
            if (state.newPassword != state.confirmPassword) {
                uiState.update { it.copy(isProcessing = false, newPasswordError = "Passwords do not match") }
                return@launch
            }

            val currentEncryptionKey = passwordManager.getEncryptionKey()
            passwordManager.setPassword(state.newPassword)
            val newEncryptionKey = passwordManager.getEncryptionKey()

            // Re-encrypt all tokens
            TAG.logD { "Starting token re-encryption" }
            tokenRepository.reEncryptAllTokens(
                oldKey = currentEncryptionKey,
                newKey = newEncryptionKey
            )

            uiState.update {
                it.copy(
                    isProcessing = false,
                    reEncryptionProgress = null,
                    successMessage = "Password changed successfully",
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = "",
                )
            }

            // Clear success message after delay
            delay(3000)
            uiState.update { it.copy(successMessage = null) }

        } catch (e: Exception) {
            this@ChangePasswordVM.logE { "Failed to change password: ${e.message}" }
            e.printStackTrace()

            uiState.update {
                it.copy(
                    isProcessing = false,
                    reEncryptionProgress = null,
                    currentPasswordError = "Failed to change password: ${e.message}"
                )
            }
        }
    }
}
