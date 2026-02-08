package com.axondragonscale.topty.widget.single

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.database.entity.Token
import com.axondragonscale.topty.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 30/12/24
 */

data class SingleTokenConfigUiState(
    val tokens: List<Token> = emptyList(),
    val searchQuery: String = "",
    val filteredTokens: List<Token> = emptyList(),
)

sealed interface SingleTokenConfigUiEvent {
    data class UpdateSearch(val query: String) : SingleTokenConfigUiEvent
    data class SelectToken(val tokenId: Long) : SingleTokenConfigUiEvent
    data object Cancel : SingleTokenConfigUiEvent
}

sealed interface SingleTokenConfigUiEffect {
    data class TokenSelected(val tokenId: Long) : SingleTokenConfigUiEffect
    data object Cancel : SingleTokenConfigUiEffect
}

class SingleTokenConfigVM(
    private val tokenRepository: TokenRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SingleTokenConfigUiState())
    val uiState = _uiState.asStateFlow()

    private val uiEffectChannel = Channel<SingleTokenConfigUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        loadTokens()
    }

    fun onEvent(event: SingleTokenConfigUiEvent) = when (event) {
        is SingleTokenConfigUiEvent.UpdateSearch -> updateSearch(event.query)
        is SingleTokenConfigUiEvent.SelectToken -> selectToken(event.tokenId)
        SingleTokenConfigUiEvent.Cancel -> cancel()
    }

    private fun loadTokens() = viewModelScope.launch(Dispatchers.IO) {
        tokenRepository.getTokens().collect { tokens ->
            _uiState.update { state ->
                state.copy(
                    tokens = tokens,
                    filteredTokens = filterTokens(tokens, state.searchQuery)
                )
            }
        }
    }

    private fun updateSearch(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredTokens = filterTokens(state.tokens, query)
            )
        }
    }

    private fun filterTokens(tokens: List<Token>, query: String): List<Token> {
        if (query.isBlank()) return tokens
        return tokens.filter { token ->
            token.issuer.contains(query, ignoreCase = true) ||
            token.name.contains(query, ignoreCase = true) ||
            token.group.contains(query, ignoreCase = true)
        }
    }

    private fun selectToken(tokenId: Long) = viewModelScope.launch(Dispatchers.IO) {
        uiEffectChannel.send(SingleTokenConfigUiEffect.TokenSelected(tokenId))
    }

    private fun cancel() = viewModelScope.launch(Dispatchers.IO) {
        uiEffectChannel.send(SingleTokenConfigUiEffect.Cancel)
    }
}
