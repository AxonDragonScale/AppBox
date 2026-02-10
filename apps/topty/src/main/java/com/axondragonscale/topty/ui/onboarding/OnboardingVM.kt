package com.axondragonscale.topty.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.repository.ToptyPrefsRepository
import com.axondragonscale.topty.security.PasswordManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 28/12/24
 */
class OnboardingVM(
    private val prefsRepository: ToptyPrefsRepository,
    private val passwordManager: PasswordManager,
) : ViewModel() {

    val uiState = MutableStateFlow(OnboardingUiState())

    private val uiEffectChannel = Channel<OnboardingUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: OnboardingUiEvent): Any = when (event) {
        is OnboardingUiEvent.NextPage -> onNextPage()
        is OnboardingUiEvent.PasswordChanged -> onPasswordChanged(event.password)
        is OnboardingUiEvent.ConfirmPasswordChanged -> onConfirmPasswordChanged(event.confirmPassword)
        is OnboardingUiEvent.SetPassword -> onSetPassword()
    }

    private fun onNextPage() {
        uiState.update { state ->
            val nextPage = state.currentPage + 1
            if (nextPage < state.totalPages) {
                state.copy(currentPage = nextPage)
            } else {
                // Reached the end, complete onboarding
                viewModelScope.launch(Dispatchers.IO) {
                    prefsRepository.hasCompletedOnboarding.set(true)
                    uiEffectChannel.send(OnboardingUiEffect.NavigateToHome)
                }
                state
            }
        }
    }



    private fun onPasswordChanged(password: String) {
        uiState.update { state ->
            val strength = passwordManager.checkPasswordStrength(password)
            val error = if (password.isEmpty()) null else passwordManager.validatePassword(password)
            state.copy(password = password, passwordStrength = strength, passwordError = error)
        }
    }

    private fun onConfirmPasswordChanged(confirmPassword: String) {
        uiState.update { state ->
            state.copy(confirmPassword = confirmPassword)
        }
    }

    private fun onSetPassword() = viewModelScope.launch(Dispatchers.IO) {
        val state = uiState.value

        // Validate password
        val passwordError = passwordManager.validatePassword(state.password)
        if (passwordError != null) {
            uiState.update { it.copy(passwordError = passwordError) }
            return@launch
        }

        // Check passwords match
        if (state.password != state.confirmPassword) {
            uiState.update { it.copy(passwordError = "Passwords do not match") }
            return@launch
        }

        passwordManager.setPassword(state.password)
        prefsRepository.hasCompletedOnboarding.set(true)

        // Password is set on last page, navigate to home
        uiEffectChannel.send(OnboardingUiEffect.NavigateToHome)
    }
}
