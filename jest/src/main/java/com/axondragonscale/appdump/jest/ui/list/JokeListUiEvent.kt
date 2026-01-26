package com.axondragonscale.appdump.jest.ui.list

sealed interface JokeListUiEvent {
    data class FavoriteToggled(val jokeId: Int, val favorite: Boolean) : JokeListUiEvent
}
