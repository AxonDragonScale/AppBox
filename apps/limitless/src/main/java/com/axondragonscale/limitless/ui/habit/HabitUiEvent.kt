package com.axondragonscale.limitless.ui.habit

import com.axondragonscale.limitless.database.entity.RecordEntity
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 17/02/26
 */
sealed interface HabitUiEvent {
    data object NavigateBack : HabitUiEvent
    data object NavigateToEditor : HabitUiEvent
    data class NavigateToDayDetail(val date: LocalDate) : HabitUiEvent
    data class SaveRecord(val record: RecordEntity) : HabitUiEvent
}
