package com.axondragonscale.jest.ui.home

import com.axondragonscale.jest.model.IJoke

/**
 * Created by Ronak Harkhani on 01/04/24
 */
data class HomeUiState(
    val joke: IJoke? = null,
    val isLoading: Boolean = true,
    val shouldAnimateJoke: Boolean = true,
    val jokeCount: Int = 0,
    val favoriteCount: Int = 0,
)
