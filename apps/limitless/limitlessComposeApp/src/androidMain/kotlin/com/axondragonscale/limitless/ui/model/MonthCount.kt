package com.axondragonscale.limitless.ui.model

/**
 * Total record count for a single calendar month.
 *
 * @param year Calendar year (e.g., 2025).
 * @param month Month number (1 = January .. 12 = December).
 * @param count Total records logged in this month.
 */
data class MonthCount(
    val year: Int,
    val month: Int,
    val count: Int,
)
