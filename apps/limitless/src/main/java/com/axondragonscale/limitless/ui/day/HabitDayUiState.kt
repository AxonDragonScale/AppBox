package com.axondragonscale.limitless.ui.day

import com.axondragonscale.limitless.database.entity.RecordEntity
import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 29/06/25
 */
data class HabitDayUiState(
    val date: LocalDate? = null,
    val habitName: String = "",
    val records: List<RecordEntity> = emptyList(),
    val isLoading: Boolean = true,
)
