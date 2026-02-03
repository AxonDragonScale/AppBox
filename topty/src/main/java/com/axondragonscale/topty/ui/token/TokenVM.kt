package com.axondragonscale.topty.ui.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.topty.core.TokenUriHandler
import com.axondragonscale.topty.repository.TokenRepository
import com.axondragonscale.topty.ui.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 14/10/24
 */
class TokenVM(
    private val tokenRoute: Route.Token,
    private val repository: TokenRepository,
): ViewModel() {

    val uiState = MutableStateFlow(TokenUiState(tokenRoute = tokenRoute))

    private val uiEffectChannel = Channel<TokenUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    init {
        initializeToken()
    }

    fun onEvent(event: TokenUiEvent) {
        when (event) {
            is TokenUiEvent.UpdateToken -> onTokenUpdated(event)
            TokenUiEvent.Save -> onSave()

            // Handled in Compose
            TokenUiEvent.NavigateBack -> Unit
        }
    }

    private fun onSave() = viewModelScope.launch(Dispatchers.IO) {
        repository.saveToken(uiState.value.token)
        uiEffectChannel.send(TokenUiEffect.NavigateToHome)
    }

    private fun onTokenUpdated(event: TokenUiEvent.UpdateToken) {
        uiState.update { it.copy(token = event.token) }
    }

    private fun initializeToken() = viewModelScope.launch(Dispatchers.IO) {
        val token = when (tokenRoute) {
            is Route.Token.Add -> TokenUriHandler.parseTokenUri(tokenRoute.uri)
            is Route.Token.Edit -> repository.getToken(tokenRoute.id)
        }
        token?.let { uiState.update { it.copy(token = token) } }
    }
}
