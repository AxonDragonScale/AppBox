package com.axondragonscale.appdump.jest.ui.home

import com.axondragonscale.appdump.jest.model.IJoke

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

}
