package com.axondragonscale.devinator.ui.setup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Created by Ronak on 11/06/26 */
class SetupVM : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: SetupUiEvent): Any = when (event) {
        is SetupUiEvent.CheckAgain -> Unit
    }
}

data class SetupUiState(
    val permissionGranted: Boolean = false,
)

sealed interface SetupUiEvent {
    data object CheckAgain : SetupUiEvent
}
