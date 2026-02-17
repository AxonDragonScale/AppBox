package com.axondragonscale.limitless.ui.home

/**
 * Created by Ronak Harkhani on 01/11/24
 */
sealed interface HomeUiEvent {
    data object NavigateToSettings : HomeUiEvent
    data object NavigateToStats : HomeUiEvent
    data object AddHabit : HomeUiEvent
    data class RecordHabit(val habitId: Long, val message: String) : HomeUiEvent
}
