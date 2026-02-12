package com.axondragonscale.npinner.model

/**
 * Created by Ronak Harkhani on 30/04/23
 */
enum class ScheduleType {
    DAY, WEEK, MONTH;

    companion object {
        val stringValues by lazy { entries.map { it.name } }

        fun fromOrdinal(ordinal: Int) = entries[ordinal]
    }
}
