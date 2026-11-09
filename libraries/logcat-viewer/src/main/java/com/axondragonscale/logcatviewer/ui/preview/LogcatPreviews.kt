package com.axondragonscale.logcatviewer.ui.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.axondragonscale.logcatviewer.model.LogEntry
import com.axondragonscale.logcatviewer.model.LogFilter
import com.axondragonscale.logcatviewer.model.LogLevel
import com.axondragonscale.logcatviewer.model.SavedView
import com.axondragonscale.logcatviewer.ui.LogcatSelection
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle
import com.axondragonscale.logcatviewer.ui.style.rememberLogcatStyle

/** Created by Ronak on 09/11/26 */

@Composable
internal fun LogcatPreviewTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        CompositionLocalProvider(
            LocalLogcatStyle provides rememberLogcatStyle(),
            content = content,
        )
    }
}

internal val PreviewLogEntries = listOf(
    LogEntry(1, "14:32:07.418", 4821, 4830, LogLevel.VERBOSE, "DevBootReceiver", "reconcile: no wireless_auto_off deadline stored"),
    LogEntry(2, "14:32:07.902", 4821, 4821, LogLevel.DEBUG, "DevSettings", "write developer_options=1 took 3 ms"),
    LogEntry(3, "14:32:08.240", 4821, 4821, LogLevel.INFO, "DevTileService", "tile list refreshed: 5 of 5 present"),
    LogEntry(4, "14:32:08.300", 4821, 4821, LogLevel.WARN, "Choreographer", "Skipped 42 frames; main thread busy"),
    LogEntry(
        5,
        "14:33:02.117",
        4821,
        4830,
        LogLevel.ERROR,
        "DevTileService",
        "requestAddTileService failed (not foreground)\n" +
            "IllegalStateException: caller not foreground\n" +
            "  at TileService.requestAddTileService:412\n" +
            "  at DevTileService.requestAdd:88",
    ),
    LogEntry(6, "14:33:02.210", 4821, 4830, LogLevel.DEBUG, "DevTileService", "foreground check failed; retry in 500 ms"),
    LogEntry(7, "14:33:02.890", 4821, 4830, LogLevel.INFO, "DevTileService", "startListening: 5 tiles present"),
)

internal val PreviewSelection = LogcatSelection(
    entry = PreviewLogEntries.last { it.level == LogLevel.ERROR },
    packageName = "com.axondragonscale.devinator",
)

internal val PreviewFilter = LogFilter(
    includePackages = setOf("com.axondragonscale.devinator"),
    excludeTags = setOf("DevAdbRepo"),
    query = "timeout",
)

internal val PreviewViews = listOf(
    SavedView(
        id = 1,
        name = "Wireless debugging",
        filter = LogFilter(
            levels = setOf(LogLevel.WARN, LogLevel.ERROR),
            includeTags = setOf("DevTileService"),
        ),
    ),
    SavedView(id = 2, name = "Crashes", filter = LogFilter(levels = setOf(LogLevel.ERROR, LogLevel.FATAL))),
    SavedView(id = 3, name = "Boot sequence", filter = LogFilter(includeTags = setOf("DevBootReceiver"))),
)
