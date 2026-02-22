package com.axondragonscale.limitless.ui.day

import com.axondragonscale.limitless.database.entity.RecordEntity

/**
 * Created by Ronak Harkhani on 29/06/25
 */
sealed interface HabitDayUiEvent {
    data object NavigateBack : HabitDayUiEvent
    data class SaveRecord(val record: RecordEntity) : HabitDayUiEvent
    data class DeleteRecord(val id: Long) : HabitDayUiEvent
}
