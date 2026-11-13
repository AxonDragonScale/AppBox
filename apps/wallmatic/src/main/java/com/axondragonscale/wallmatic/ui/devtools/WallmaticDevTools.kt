package com.axondragonscale.wallmatic.ui.devtools

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axondragonscale.commons.devtools.DevToolsScreen
import com.axondragonscale.wallmatic.BuildConfig
import com.axondragonscale.wallmatic.core.SyncManager
import com.axondragonscale.wallmatic.core.readWallpaperBounds
import com.axondragonscale.wallmatic.core.viewport
import com.axondragonscale.wallmatic.core.wallpaperApplySize
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.repository.AppPrefsRepository
import com.axondragonscale.wallmatic.repository.WallmaticRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.util.Locale

/**
 * Wallmatic-specific developer tools screen.
 * Uses the common DevToolsScreen and adds Wallmatic-specific tools.
 */
@Composable
fun WallmaticDevTools(
    modifier: Modifier = Modifier,
    appPrefsRepository: AppPrefsRepository = koinInject(),
    syncManager: SyncManager = koinInject(),
    repository: WallmaticRepository = koinInject(),
) {
    val context = LocalContext.current
    val fastAutoCycle by appPrefsRepository.fastAutoCycleFlow
        .collectAsStateWithLifecycle(initialValue = false)

    val cropReadout by produceState<CropReadout?>(initialValue = null) {
        appPrefsRepository.configFlow
            .map { it.homeConfig.currentWallpaperId }
            .distinctUntilChanged()
            .collect { wallpaperId ->
                value = withContext(Dispatchers.IO) {
                    wallpaperId.takeIf { it != -1 }
                        ?.let { repository.getWallpaper(it) }
                        ?.let { cropReadout(context, it) }
                }
            }
    }

    DevToolsScreen(
        modifier = modifier,
        title = "Wallmatic Dev Tools",
        subtitle = "Debug utilities for Wallmatic",
        customToolsBuilder = {
            // App Info Section
            section("App Info", Icons.Rounded.Storage)
            info("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            info("Package", BuildConfig.APPLICATION_ID)

            // Wallpaper Crop Section
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

            // Wallmatic Tools Section
            section("Wallmatic Tools", Icons.Rounded.Sync)

            action(
                title = "Sync Albums",
                subtitle = "Synchronize all albums with filesystem",
                icon = Icons.Rounded.Sync,
                onClick = { withContext(Dispatchers.IO) { syncManager.syncAlbums() } }
            )

            toggle(
                title = "Fast Auto-Cycle",
                subtitle = "Use 1 minute interval instead of configured duration",
                icon = Icons.Rounded.Timer,
                checked = fastAutoCycle,
            ) { enabled ->
                CoroutineScope(Dispatchers.IO).launch {
                    appPrefsRepository.setFastAutoCycle(enabled)
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

private fun cropReadout(context: Context, wallpaper: Wallpaper): CropReadout {
    val target = wallpaperApplySize(context)
    val crop = readWallpaperBounds(context, wallpaper.uri.toUri())?.let { source ->
        viewport(
            sourceWidth = source.width,
            sourceHeight = source.height,
            focalX = wallpaper.cropFocalX,
            focalY = wallpaper.cropFocalY,
            zoom = wallpaper.cropZoom,
            targetAspect = target.width.toFloat() / target.height,
        )
    }
    return CropReadout(
        fileName = fileNameTail(wallpaper.uri),
        focalX = wallpaper.cropFocalX,
        focalY = wallpaper.cropFocalY,
        zoom = wallpaper.cropZoom,
        crop = crop,
        target = target,
    )
}

private fun fileNameTail(uri: String): String =
    Uri.parse(uri).lastPathSegment?.let(Uri::decode)?.takeLast(40).orEmpty().ifEmpty { uri.takeLast(40) }
