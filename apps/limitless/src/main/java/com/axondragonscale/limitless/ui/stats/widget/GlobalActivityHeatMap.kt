package com.axondragonscale.limitless.ui.stats.widget

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.axondragonscale.commons.compose.StatCard
import com.axondragonscale.commons.today
import com.axondragonscale.limitless.ui.common.HeatMapGrid
import com.axondragonscale.limitless.ui.stats.model.GlobalHeatMapData
import com.axondragonscale.limitless.ui.theme.LimitlessTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Created by Ronak Harkhani on 23/07/25
 *
 * A card showing the global activity heat map — a GitHub-style contribution grid covering
 * the last ~6 months.
 *
 * Each cell represents one day. Color intensity is determined by how many *distinct habits*
 * were recorded that day relative to [GlobalHeatMapData.totalHabits] (the maximum possible).
 * This reflects breadth of engagement rather than raw record volume: a user who did 5 different
 * habits once each gets a brighter cell than one who logged a single habit 8 times.
 *
 * The grid uses the theme's primary color so it feels cohesive with the app rather than
 * habit-specific. All grid mechanics (week building, month labels, scroll-to-end, cell
 * coloring) are delegated to [HeatMapGrid].
 *
 * An empty grid is shown when there are no records yet — the shell remains visible so the
 * page layout doesn't collapse.
 */
@Composable
fun GlobalActivityHeatMap(
    modifier: Modifier = Modifier,
    data: GlobalHeatMapData,
) {
    val primary = MaterialTheme.colorScheme.primary

    StatCard(
        modifier = modifier,
        title = "Activity",
    ) {
        HeatMapGrid(
            dayCounts = data.dayCounts,
            maxCount = data.totalHabits,
            color = primary,
            highlightCurrentDay = true,
        )
    }
}

// region Previews

@Preview(name = "Light — With Data", showBackground = true)
@Preview(name = "Dark — With Data", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WithDataPreview() {
    LimitlessTheme {
        val dayCounts = buildMap {
            for (i in 0 until 180) {
                val date = LocalDate.today().minus(i, DateTimeUnit.DAY)
                val count = when {
                    i % 7 == 0 -> 5
                    i % 3 == 0 -> 3
                    i % 5 == 0 -> 1
                    i % 2 == 0 -> 4
                    else -> 0
                }
                if (count > 0) put(date, count)
            }
        }
        GlobalActivityHeatMap(
            modifier = Modifier.padding(16.dp),
            data = GlobalHeatMapData(
                dayCounts = dayCounts,
                maxCount = 5,
                totalHabits = 5,
            ),
        )
    }
}

@Preview(name = "Light — Empty", showBackground = true)
@Preview(name = "Dark — Empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() {
    LimitlessTheme {
        GlobalActivityHeatMap(
            modifier = Modifier.padding(16.dp),
            data = GlobalHeatMapData(
                dayCounts = emptyMap(),
                maxCount = 0,
                totalHabits = 3,
            ),
        )
    }
}

@Preview(name = "Light — Sparse", showBackground = true)
@Preview(name = "Dark — Sparse", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SparsePreview() {
    LimitlessTheme {
        val dayCounts = buildMap {
            for (i in listOf(1, 3, 8, 15, 22, 30, 45, 60)) {
                put(LocalDate.today().minus(i, DateTimeUnit.DAY), (1..4).random())
            }
        }
        GlobalActivityHeatMap(
            modifier = Modifier.padding(16.dp),
            data = GlobalHeatMapData(
                dayCounts = dayCounts,
                maxCount = 4,
                totalHabits = 4,
            ),
        )
    }
}

// endregion
