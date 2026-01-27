package com.axondragonscale.jest.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.appdump.jest.repository.JokeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 07/04/24
 */

class FavoritesVM(
    repository: JokeRepository,
) : JokeListVM(JokeListType.Favorites, repository)

class HistoryVM(
    repository: JokeRepository,
) : JokeListVM(JokeListType.History, repository)

sealed class JokeListVM(
    private val listType: JokeListType,
    private val repository: JokeRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(JokeListUiState(listOf()))

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val jokesFlow = when (listType) {
                JokeListType.History -> repository.getAllJokes()
                JokeListType.Favorites -> repository.getFavoriteJokes()
            }
            jokesFlow.collect { jokes ->
                uiState.update {
                    JokeListUiState(jokes)
                }
            }
        }
    }

    fun onEvent(event: JokeListUiEvent) = viewModelScope.launch(Dispatchers.IO) {
        when (event) {
            is JokeListUiEvent.FavoriteToggled -> {
                if (event.favorite) repository.addToFavorites(event.jokeId)
                else repository.removeFromFavorites(event.jokeId)
            }
        }
    }

}
