package com.axondragonscale.wallmatic.ui.devtools

import android.graphics.Rect
import android.net.Uri
import android.util.Size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.wallmatic.BuildConfig
import com.axondragonscale.wallmatic.background.UpdateCoordinator
import com.axondragonscale.wallmatic.core.WallpaperApplier
import com.axondragonscale.wallmatic.core.viewport
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.Interval
import com.axondragonscale.wallmatic.repository.ConfigRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

@Composable
fun WallmaticDevTools(
    modifier: Modifier = Modifier,
    configRepository: ConfigRepository = koinInject(),
    updateCoordinator: UpdateCoordinator = koinInject(),
    repository: WallmaticRepository = koinInject(),
    applier: WallpaperApplier = koinInject(),
) {
    val fastCycle by configRepository.configFlow
        .map { it.interval <= 1.minutes }
        .distinctUntilChanged()
        .collectAsStateWithLifecycle(initialValue = false)

    val cropReadout by produceState<CropReadout?>(initialValue = null) {
        configRepository.configFlow
            .map { it.home.currentWallpaperId }
            .distinctUntilChanged()
            .collect { wallpaperId ->
                value = withContext(Dispatchers.IO) {
                    wallpaperId?.let { repository.getWallpaper(it) }
                        ?.let { cropReadout(applier, it) }
                }
            }
    }

    DevToolsScreen(
        modifier = modifier,
        title = "Wallmatic Dev Tools",
        subtitle = "Debug utilities for Wallmatic",
        customToolsBuilder = {
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)

            section("Active Home Wallpaper", Icons.Rounded.CropFree)
            val readout = cropReadout
            if (readout == null) {
                info("Wallpaper", "None")
            } else {
                info("File", readout.fileName)
                info("Focal", String.format(Locale.US, "%.2f / %.2f", readout.focalX, readout.focalY))
                info("Zoom", String.format(Locale.US, "%.2fx", readout.zoom))
                info("Crop rect", readout.cropText)
                info("Apply target", "${readout.target.width} × ${readout.target.height}")
            }

            section("Wallmatic Tools", Icons.Rounded.Sync)

            action(
                title = "Sync Albums",
                subtitle = "Synchronize all albums with filesystem",
                icon = Icons.Rounded.Sync,
                onClick = {
                    withContext(Dispatchers.IO) { updateCoordinator.refresh() }
                }
            )

            toggle(
                title = "Fast Auto-Cycle",
                subtitle = "Use 1 minute interval instead of configured duration",
                icon = Icons.Rounded.Timer,
                checked = fastCycle,
            ) { enabled ->
                CoroutineScope(Dispatchers.IO).launch {
                    configRepository.setInterval(if (enabled) 1.minutes else Interval.default)
                    updateCoordinator.reschedule()
                }
            }
        }
    )
}

private class CropReadout(
    val fileName: String,
    val focalX: Float,
    val focalY: Float,
    val zoom: Float,
    val crop: Rect?,
    val target: Size,
) {
    val cropText: String
        get() = crop?.let { "${it.left}, ${it.top} · ${it.width()} × ${it.height()}" } ?: "Unreadable"
}

private fun cropReadout(applier: WallpaperApplier, wallpaper: Wallpaper): CropReadout {
    val target = applier.applySize()
    val crop = applier.sourceSize(wallpaper.uri)?.let { source ->
        viewport(
            sourceWidth = source.width,
            sourceHeight = source.height,
            crop = wallpaper.crop,
            targetAspect = target.width.toFloat() / target.height,
        )
    }
    return CropReadout(
        fileName = fileNameTail(wallpaper.uri),
        focalX = wallpaper.crop.focalX,
        focalY = wallpaper.crop.focalY,
        zoom = wallpaper.crop.zoom,
        crop = crop,
        target = target,
    )
}

private fun fileNameTail(uri: String): String =
    Uri.parse(uri).lastPathSegment?.let(Uri::decode)?.takeLast(40).orEmpty().ifEmpty { uri.takeLast(40) }
