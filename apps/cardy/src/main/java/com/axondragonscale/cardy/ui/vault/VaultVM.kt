/** Created by Ronak on 05/30/26 */
package com.axondragonscale.cardy.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.cardy.database.entity.CardEntity
import com.axondragonscale.cardy.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VaultVM(
    private val cardRepository: CardRepository,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState = _uiState.asStateFlow()

    // endregion

    init {
        observeCards()
    }

    // region Event Handling

    fun onEvent(event: VaultUiEvent): Any = when (event) {
        is VaultUiEvent.Authenticated -> onAuthenticated()
        is VaultUiEvent.AuthFailed -> onAuthFailed(event.message)
        is VaultUiEvent.DeleteCard -> deleteCard(event.cardId)
        is VaultUiEvent.NavigateToDetail -> Unit // Handled in composable
        is VaultUiEvent.NavigateBack -> Unit // Handled in composable
    }

    // endregion

    private fun observeCards() = viewModelScope.launch {
        cardRepository.getAllCards().collect { cards ->
            _uiState.update { it.copy(cards = cards) }
        }
    }

    private fun onAuthenticated() {
        _uiState.update { it.copy(isAuthenticated = true) }
    }

    private fun onAuthFailed(message: String) {
        _uiState.update { it.copy(authError = message) }
    }

    private fun deleteCard(cardId: Long) = viewModelScope.launch {
        cardRepository.deleteCard(cardId)
    }
}

data class VaultUiState(
    val isAuthenticated: Boolean = false,
    val cards: List<CardEntity> = emptyList(),
    val authError: String? = null,
)

sealed interface VaultUiEvent {
    data object Authenticated : VaultUiEvent
    data class AuthFailed(val message: String) : VaultUiEvent
    data class DeleteCard(val cardId: Long) : VaultUiEvent
    data class NavigateToDetail(val cardId: Long) : VaultUiEvent
    data object NavigateBack : VaultUiEvent
}
