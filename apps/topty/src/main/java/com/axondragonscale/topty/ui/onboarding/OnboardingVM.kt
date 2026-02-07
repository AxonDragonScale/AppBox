package com.axondragonscale.topty.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.repository.ToptyPrefsRepository
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
) : ViewModel() {

    val uiState = MutableStateFlow(OnboardingUiState())

    private val uiEffectChannel = Channel<OnboardingUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: OnboardingUiEvent): Any = when (event) {
        OnboardingUiEvent.NextPage -> onNextPage()
        OnboardingUiEvent.PreviousPage -> onPreviousPage()
        OnboardingUiEvent.SkipOnboarding -> onSkipOnboarding()
        OnboardingUiEvent.NavigateToQRScanner -> onNavigateToQRScanner()
        OnboardingUiEvent.NavigateToManualEntry -> onNavigateToManualEntry()
        OnboardingUiEvent.NavigateToImport -> onNavigateToImport()
    }

    private fun onNextPage() {
        uiState.update { state ->
            val nextPage = state.currentPage + 1
            if (nextPage < state.totalPages) {
                state.copy(currentPage = nextPage)
            } else {
                state
            }
        }
    }

    private fun onPreviousPage() {
        uiState.update { state ->
            val prevPage = state.currentPage - 1
            if (prevPage >= 0) {
                state.copy(currentPage = prevPage)
            } else {
                state
            }
        }
    }

    private fun onSkipOnboarding() = viewModelScope.launch(Dispatchers.IO) {
        prefsRepository.hasCompletedOnboarding.set(true)
        uiEffectChannel.send(OnboardingUiEffect.NavigateToHome)
    }

    private fun onNavigateToQRScanner() = viewModelScope.launch(Dispatchers.IO) {
        prefsRepository.hasCompletedOnboarding.set(true)
        uiEffectChannel.send(OnboardingUiEffect.NavigateToQRScanner)
    }

    private fun onNavigateToManualEntry() = viewModelScope.launch(Dispatchers.IO) {
        prefsRepository.hasCompletedOnboarding.set(true)
        uiEffectChannel.send(OnboardingUiEffect.NavigateToAddToken)
    }

    private fun onNavigateToImport() = viewModelScope.launch(Dispatchers.IO) {
        prefsRepository.hasCompletedOnboarding.set(true)
        uiEffectChannel.send(OnboardingUiEffect.NavigateToBackupRestore)
    }
}
