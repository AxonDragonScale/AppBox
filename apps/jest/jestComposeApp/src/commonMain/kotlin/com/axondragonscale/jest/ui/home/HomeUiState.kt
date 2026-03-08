package com.axondragonscale.jest.ui.home

import com.axondragonscale.jest.model.IJoke

/**
 * Created by Ronak Harkhani on 01/04/24
 */
sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Success(
        val joke: IJoke,
        val shouldAnimateJoke: Boolean = true,
    ) : HomeUiState

}
