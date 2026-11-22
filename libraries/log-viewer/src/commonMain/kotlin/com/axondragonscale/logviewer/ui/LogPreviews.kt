package com.axondragonscale.logviewer.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import com.axondragonscale.logviewer.model.LogId
import com.axondragonscale.logviewer.model.LogLevel
import com.axondragonscale.logviewer.model.LogMeta
import com.axondragonscale.logviewer.model.LogRecord
import com.axondragonscale.logviewer.source.LogCapabilities
import com.axondragonscale.logviewer.source.LogFacet
import com.axondragonscale.logviewer.source.LogFacets
import com.axondragonscale.logviewer.source.LogQuery
import com.axondragonscale.logviewer.source.LogRule
import com.axondragonscale.logviewer.ui.sheets.LogFilterSheet
import com.axondragonscale.logviewer.ui.sheets.RuleDraft
import com.axondragonscale.logviewer.ui.sheets.RuleSheet
import kotlin.time.Instant

/** Created by Ronak Harkhani on 11/22/26 */

private val PreviewCapabilities = LogCapabilities(
    live = true,
    history = true,
    pid = true,
    tid = true,
    packageName = true,
    sessions = true,
    clear = true,
)

private val PreviewRecords = listOf(
    LogRecord(
        id = LogId("5"),
        time = Instant.fromEpochMilliseconds(1_795_289_064_042),
        level = LogLevel.WARN,
        tag = "UpdateCoordinator",
        message = "Skipped, waiting for the next cycle. reason: Config is not initialised",
        meta = LogMeta(pid = 5120, tid = 5634, packageName = "com.example.app", session = "a1b2c3"),
    ),
    LogRecord(
        id = LogId("4"),
        time = Instant.fromEpochMilliseconds(1_795_289_064_035),
        level = LogLevel.DEBUG,
        tag = "WallpaperCycler",
        message = "Wallpaper update skipped. isInitialised: false",
        meta = LogMeta(pid = 5120, tid = 5634, packageName = "com.example.app", session = "a1b2c3"),
    ),
    LogRecord(
        id = LogId("3"),
        time = Instant.fromEpochMilliseconds(1_795_289_063_928),
        level = LogLevel.ERROR,
        tag = "AlbumSyncer",
        message = "Handled IOException: /storage/emulated/0/Pictures is gone\n" +
            "java.io.FileNotFoundException: /storage/emulated/0/Pictures\n" +
            "\tat com.example.sync(AlbumSyncer.kt:42)",
        meta = LogMeta(pid = 5120, tid = 5670, packageName = "com.example.app", session = "a1b2c3"),
    ),
    LogRecord(
        id = LogId("2"),
        time = Instant.fromEpochMilliseconds(1_795_289_062_214),
        level = LogLevel.INFO,
        tag = "WallmaticActivity",
        message = "onCreate",
        meta = LogMeta(pid = 5120, tid = 5120, packageName = "com.example.app", session = "a1b2c3"),
    ),
)

@Composable
private fun PreviewScreen(
    records: List<LogRecord> = PreviewRecords,
    dark: Boolean = true,
    paused: Boolean = false,
    pendingCount: Int = 0,
) {
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        LogScreen(
            state = LogUiState(
                capabilities = PreviewCapabilities,
                hasLoaded = true,
            ),
            entries = records,
            listState = rememberLazyListState(),
            title = "Logcat",
            caption = "all buffers",
            paused = paused,
            pendingCount = pendingCount,
            wrap = false,
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onTogglePause = {},
            onToggleWrap = {},
            onJumpToNewest = {},
            onBack = {},
            onShare = {},
            onCopy = {},
        )
    }
}

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun LogScreenDarkPreview() = PreviewScreen()

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_NO)
@Composable
private fun LogScreenLightPreview() = PreviewScreen(dark = false)

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun LogScreenEmptyPreview() = PreviewScreen(records = emptyList())

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun LogScreenPausedPreview() = PreviewScreen(paused = true, pendingCount = 7)

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun LogFilterSheetPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LogFilterSheet(
            state = LogUiState(
                capabilities = PreviewCapabilities,
                query = LogQuery(
                    rules = listOf(
                        LogRule(LogFacet.TAG, "WallpaperCycler", include = true),
                        LogRule(LogFacet.TAG, "Choreographer", include = false),
                        LogRule(LogFacet.PACKAGE, "com.example.app", include = false),
                    ),
                ),
            ),
            facets = LogFacets(
                tags = listOf("AlbumSyncer", "UpdateCoordinator", "WallpaperCycler"),
                packages = listOf("com.example.app", "com.example.sync"),
            ),
            lineCount = 128,
            onEvent = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun RuleSheetPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        RuleSheet(
            draft = RuleDraft(LogFacet.TAG, include = true),
            suggestions = listOf(
                "AlbumSyncer",
                "Choreographer",
                "TickScheduler",
                "UpdateCoordinator",
                "WallpaperCycler",
            ),
            onDismiss = {},
            onAdd = {},
        )
    }
}
