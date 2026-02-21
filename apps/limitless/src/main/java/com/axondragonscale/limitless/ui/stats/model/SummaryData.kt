package com.axondragonscale.limitless.ui.stats.model

import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Data for the Summary Stats grid widget.
 * Displays key aggregate metrics for the habit at a glance.
 *
 * @param totalRecords Total number of records logged for this habit.
 * @param activeDays Number of unique days with at least one record.
 * @param avgPerActiveDay Average records per active day.
 * @param trackingSince Date of the first recorded entry, or null if no records exist.
 */
data class SummaryData(
    val totalRecords: Int,
    val activeDays: Int,
    val avgPerActiveDay: Float,
    val trackingSince: LocalDate?,
)
