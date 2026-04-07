package com.axondragonscale.jest.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axondragonscale.jest.repository.JokeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ronak Harkhani on 24/10/24
 */
class LibraryVM(
    private val repository: JokeRepository,
) : ViewModel() {

    private val activeFilter = MutableStateFlow(LibraryFilter.All)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = activeFilter
        .flatMapLatest { filter ->
            when (filter) {
                LibraryFilter.All -> repository.getAllJokes()
                LibraryFilter.Favorites -> repository.getFavoriteJokes()
            }
        }
        .combine(activeFilter) { jokes, filter ->
            LibraryUiState(
                jokes = jokes,
                activeFilter = filter,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LibraryUiState(),
        )

    fun onEvent(event: LibraryUiEvent): Any = when (event) {
        is LibraryUiEvent.FilterChanged -> activeFilter.update { event.filter }
        is LibraryUiEvent.FavoriteToggled -> handleFavoriteToggled(event)
    }

    private fun handleFavoriteToggled(event: LibraryUiEvent.FavoriteToggled) =
        viewModelScope.launch(Dispatchers.Default) {
            if (event.favorite) repository.addToFavorites(event.jokeId)
            else repository.removeFromFavorites(event.jokeId)
        }
}
