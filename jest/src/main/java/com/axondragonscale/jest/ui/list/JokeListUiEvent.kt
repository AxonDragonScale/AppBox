package com.axondragonscale.jest.ui.list

sealed interface JokeListUiEvent {
    data class FavoriteToggled(val jokeId: Int, val favorite: Boolean) : JokeListUiEvent
}
