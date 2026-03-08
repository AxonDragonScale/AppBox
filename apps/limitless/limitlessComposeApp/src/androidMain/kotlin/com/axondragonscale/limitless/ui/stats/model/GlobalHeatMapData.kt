package com.axondragonscale.limitless.ui.stats.model

import kotlinx.datetime.LocalDate

/**
 * Created by Ronak Harkhani on 28/07/25
 *
 * Data for the global Activity Heat Map section.
 *
 * Unlike per-habit heat maps, each cell represents how many *distinct habits* were recorded
 * on that day rather than how many times a single habit was logged. This gives a cross-habit
 * view of overall engagement.
 *
 * @param dayCounts  Map of date → number of distinct habits recorded that day.
 * @param maxCount   The maximum value in [dayCounts], used to normalise cell color intensity.
 *                   Equal to [totalHabits] when at least one day had every habit recorded.
 * @param totalHabits Total number of active habits, used as the upper-bound for normalisation.
 */
data class GlobalHeatMapData(
    val dayCounts: Map<LocalDate, Int>,
    val maxCount: Int,
    val totalHabits: Int,
)
