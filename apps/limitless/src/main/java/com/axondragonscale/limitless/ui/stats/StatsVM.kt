package com.axondragonscale.limitless.ui.stats

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by Ronak Harkhani on 03/11/24
 */
class StatsVM : ViewModel() {

    val uiState = MutableStateFlow(StatsUiState())

    fun onEvent(event: StatsUiEvent) {

    }

}
