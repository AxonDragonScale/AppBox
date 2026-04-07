package com.axondragonscale.jest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.jest.model.OnePartJoke
import com.axondragonscale.jest.model.TwoPartJoke
import com.axondragonscale.jest.repository.JokeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 01/04/24
 */
class HomeVM(
    private val repository: JokeRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())

    init {
        loadCurrentJoke()
        observeCounts()
    }

    fun onEvent(event: HomeUiEvent): Any = when (event) {
        is HomeUiEvent.JokeAnimationComplete -> onJokeAnimationComplete()
        is HomeUiEvent.NewJoke -> loadNewJoke()
        is HomeUiEvent.FavoriteToggled -> handleFavoriteToggled(event)
        is HomeUiEvent.OpenCategoryFilter -> Unit // Handled in Compose
        is HomeUiEvent.OpenFlagsFilter -> Unit    // Handled in Compose
        is HomeUiEvent.CloseFilter -> Unit        // Handled in Compose
    }

    private fun loadCurrentJoke() = viewModelScope.launch(Dispatchers.Default) {
        uiState.update { it.copy(isLoading = true) }
        val joke = repository.getCurrentJoke()
        uiState.update { it.copy(joke = joke, isLoading = false, shouldAnimateJoke = true) }
    }

    private fun loadNewJoke() = viewModelScope.launch(Dispatchers.Default) {
        uiState.update { it.copy(isLoading = true, joke = null) }
        val joke = repository.getNewJoke()
        uiState.update { it.copy(joke = joke, isLoading = false, shouldAnimateJoke = true) }
    }

    private fun onJokeAnimationComplete() {
        uiState.update { it.copy(shouldAnimateJoke = false) }
    }

    private fun handleFavoriteToggled(event: HomeUiEvent.FavoriteToggled) =
        viewModelScope.launch(Dispatchers.Default) {
            val updatedJoke = when (val joke = event.joke) {
                is OnePartJoke -> joke.copy(favorite = event.favorite)
                is TwoPartJoke -> joke.copy(favorite = event.favorite)
            }
            launch {
                if (event.favorite) repository.addToFavorites(updatedJoke.id)
                else repository.removeFromFavorites(updatedJoke.id)
            }
            uiState.update { it.copy(joke = updatedJoke) }
        }

    private fun observeCounts() {
        repository.getJokeCount()
            .onEach { count -> uiState.update { it.copy(jokeCount = count) } }
            .launchIn(viewModelScope)

        repository.getFavoriteCount()
            .onEach { count -> uiState.update { it.copy(favoriteCount = count) } }
            .launchIn(viewModelScope)
    }
}
