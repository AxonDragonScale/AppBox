package com.axondragonscale.limitless.ui.stats.model

/**
 * Created by Ronak Harkhani on 21/07/25
 *
 * Data for the Weekly Pattern bar chart widget.
 * Shows average records per day of week to reveal consistency patterns.
 *
 * @param dayAverages Average record count per day. Size = 7, index 0 = Monday .. 6 = Sunday.
 */
data class WeeklyPatternData(
    val dayAverages: List<Float>,
)
