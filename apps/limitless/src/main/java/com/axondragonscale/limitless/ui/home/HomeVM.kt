package com.axondragonscale.limitless.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by Ronak Harkhani on 01/11/24
 */
class HomeVM : ViewModel() {

    val uiState = MutableStateFlow(HomeUiState())

    fun onEvent(event: HomeUiEvent) = when (event) {

        // Handled in Compose
        HomeUiEvent.NavigateToSettings -> Unit
        HomeUiEvent.NavigateToStats -> Unit
        HomeUiEvent.AddHabit -> Unit
    }

}
