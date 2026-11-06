package com.axondragonscale.devinator.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Created by Ronak on 11/06/26 */
class HomeVM : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent): Any = Unit
}

class HomeUiState

sealed interface HomeUiEvent
