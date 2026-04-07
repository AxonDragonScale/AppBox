package com.axondragonscale.jest.ui.home

import com.axondragonscale.jest.model.IJoke

/**
 * Created by Ronak Harkhani on 01/04/24
 */
sealed interface HomeUiEvent {

    data object JokeAnimationComplete : HomeUiEvent

    data object NewJoke : HomeUiEvent

    data class FavoriteToggled(
        val joke: IJoke,
        val favorite: Boolean,
    ) : HomeUiEvent

    data object OpenCategoryFilter : HomeUiEvent

    data object OpenFlagsFilter : HomeUiEvent

    data object CloseFilter : HomeUiEvent

}
