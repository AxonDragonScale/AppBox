package com.axondragonscale.limitless.ui.home

import com.axondragonscale.limitless.database.entity.RecordEntity

/**
 * Created by Ronak Harkhani on 01/11/24
 */
sealed interface HomeUiEvent {
    data object NavigateToSettings : HomeUiEvent
    data object NavigateToStats : HomeUiEvent
    data object AddHabit : HomeUiEvent
    data class NavigateToHabit(val habitId: Long) : HomeUiEvent
    data class SaveRecord(val record: RecordEntity) : HomeUiEvent
}
