package com.axondragonscale.jest.ui.library

/**
 * Created by Ronak Harkhani on 24/10/24
 */
sealed interface LibraryUiEvent {
    data class FilterChanged(val filter: LibraryFilter) : LibraryUiEvent
    data class FavoriteToggled(val jokeId: Int, val favorite: Boolean) : LibraryUiEvent
}
