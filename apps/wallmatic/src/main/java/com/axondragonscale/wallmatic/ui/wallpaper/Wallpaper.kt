package com.axondragonscale.wallmatic.ui.wallpaper

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.axondragonscale.wallmatic.core.MAX_CROP_ZOOM
import com.axondragonscale.wallmatic.core.MIN_CROP_ZOOM
import com.axondragonscale.wallmatic.database.entity.Wallpaper
import com.axondragonscale.wallmatic.model.crop
import com.axondragonscale.wallmatic.ui.component.HorizontalRule
import com.axondragonscale.wallmatic.ui.component.StateMessage
import com.axondragonscale.wallmatic.ui.component.TextAction
import com.axondragonscale.wallmatic.ui.component.WallmaticTopBar
import com.axondragonscale.wallmatic.ui.component.wallpaperModel
import com.axondragonscale.wallmatic.ui.theme.WallmaticTextStyles
import com.axondragonscale.wallmatic.ui.theme.WallmaticTheme
import com.axondragonscale.wallmatic.ui.theme.wallmatic
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Created by Ronak Harkhani on 11/07/24
 */

@Composable
fun Wallpaper(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val vm: WallpaperVM = koinViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val back: () -> Unit = {
        if (uiState.mode == WallpaperMode.Edit) {
            vm.onEvent(WallpaperUiEvent.Back)
        } else {
            navController.popBackStack()
        }
    }

    BackHandler(enabled = uiState.mode == WallpaperMode.Edit, onBack = back)

    Wallpaper(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                WallpaperUiEvent.Back -> back()
                else -> vm.onEvent(event)
            }
        },
    )
}

@Composable
private fun Wallpaper(
    modifier: Modifier = Modifier,
    uiState: WallpaperUiState,
    onEvent: (WallpaperUiEvent) -> Unit,
) {
    val wallpaper = uiState.wallpaper

    when {
        uiState.isLoading -> Message(title = "LOADING")

        uiState.isMissing || wallpaper == null -> Message(
            title = "WALLPAPER NOT FOUND",
            description = "This wallpaper may have been deleted.",
        )

        else -> Content(
            modifier = modifier,
            uiState = uiState,
            wallpaper = wallpaper,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    uiState: WallpaperUiState,
    wallpaper: Wallpaper,
    onEvent: (WallpaperUiEvent) -> Unit,
) {
    val mode = uiState.mode

    var sourceAspect by remember(wallpaper.uri) { mutableStateOf<Float?>(null) }
    var chromeVisible by remember { mutableStateOf(mode == WallpaperMode.Edit) }
    var chromeTouches by remember { mutableIntStateOf(0) }
    var isGesturing by remember { mutableStateOf(false) }

    val chromeAlpha by animateFloatAsState(
        targetValue = if (chromeVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "chrome",
    )
    val guideAlpha by animateFloatAsState(
        targetValue = if (isGesturing) 1f else 0f,
        animationSpec = if (isGesturing) snap() else tween(durationMillis = 400),
        label = "guides",
    )

    // A mode switch starts a fresh chrome timer; edit mode always opens with chrome.
    LaunchedEffect(mode) {
        if (mode == WallpaperMode.Edit) chromeVisible = true
        chromeTouches++
    }

    LaunchedEffect(chromeVisible, chromeTouches) {
        if (!chromeVisible) return@LaunchedEffect
        delay(3_000)
        chromeVisible = false
    }

    fun touch() {
        chromeTouches++
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val viewportAspect = constraints.maxWidth.toFloat() / constraints.maxHeight.toFloat()
        val geometry = sourceAspect?.let { cropGeometry(it, viewportAspect) }

        when (mode) {
            WallpaperMode.View -> ViewerImage(
                wallpaper = wallpaper,
                onTap = { chromeVisible = !chromeVisible },
            )

            WallpaperMode.Edit -> {
                CropViewport(
                    modifier = Modifier.fillMaxSize(),
                    uri = wallpaper.uri,
                    sourceAspect = sourceAspect,
                    zoom = uiState.draftZoom,
                    focalX = uiState.draftFocalX,
                    focalY = uiState.draftFocalY,
                    onSourceAspect = { sourceAspect = it },
                    onGestureStart = {
                        chromeVisible = false
                        isGesturing = true
                    },
                    onGestureEnd = { isGesturing = false },
                    onTap = { chromeVisible = !chromeVisible },
                    onZoom = { onEvent(WallpaperUiEvent.ZoomChanged(it)) },
                    onFocus = { x, y -> onEvent(WallpaperUiEvent.FocusChanged(x, y)) },
                )

                ThirdsGuides(alpha = guideAlpha)
            }
        }

        if (chromeAlpha > 0f) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .alpha(chromeAlpha)
                    .background(MaterialTheme.wallmatic.paper)
                    .statusBarsPadding(),
            ) {
                WallmaticTopBar(
                    title = if (mode == WallpaperMode.Edit) "Adjust" else "Wallpaper",
                    onBack = { onEvent(WallpaperUiEvent.Back) },
                    trailing = {
                        if (mode == WallpaperMode.Edit) {
                            TextAction(
                                text = "Done",
                                enabled = uiState.isDirty && !uiState.isSaving,
                                onClick = { onEvent(WallpaperUiEvent.Save) },
                            )
                        } else {
                            TextAction(
                                text = "Adjust",
                                onClick = { onEvent(WallpaperUiEvent.EnterEdit) },
                            )
                        }
                    },
                )

                HorizontalRule(color = MaterialTheme.wallmatic.ink)
            }

            if (mode == WallpaperMode.Edit) {
                EditorFooter(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .alpha(chromeAlpha),
                    uiState = uiState,
                    onZoomChange = { target ->
                        touch()
                        val zoom = target.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM)
                        onEvent(WallpaperUiEvent.ZoomChanged(zoom))
                        geometry?.let { crop ->
                            val x = clampFocal(uiState.draftFocalX, crop.fractionX, zoom)
                            val y = clampFocal(uiState.draftFocalY, crop.fractionY, zoom)
                            if (x != uiState.draftFocalX || y != uiState.draftFocalY) {
                                onEvent(WallpaperUiEvent.FocusChanged(x, y))
                            }
                        }
                    },
                    onCenter = {
                        touch()
                        onEvent(WallpaperUiEvent.Center)
                    },
                    onReset = {
                        touch()
                        onEvent(WallpaperUiEvent.Reset)
                    },
                )
            }
        }
    }
}

@Composable
private fun ViewerImage(
    modifier: Modifier = Modifier,
    wallpaper: Wallpaper,
    onTap: () -> Unit,
) {
    AsyncImage(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        model = wallpaperModel(uri = wallpaper.uri, crop = wallpaper.crop),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun CropViewport(
    modifier: Modifier = Modifier,
    uri: String,
    sourceAspect: Float?,
    zoom: Float,
    focalX: Float,
    focalY: Float,
    onSourceAspect: (Float) -> Unit,
    onGestureStart: () -> Unit,
    onGestureEnd: () -> Unit,
    onTap: () -> Unit,
    onZoom: (Float) -> Unit,
    onFocus: (Float, Float) -> Unit,
) {
    val currentZoom by rememberUpdatedState(zoom)
    val currentFocalX by rememberUpdatedState(focalX)
    val currentFocalY by rememberUpdatedState(focalY)

    Box(modifier = modifier) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (sourceAspect == null) {
                        alpha = 0f
                    } else {
                        alpha = 1f
                        applyCropTransform(sourceAspect, zoom, focalX, focalY)
                    }
                },
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val image = state.result.image
                if (image.height > 0) onSourceAspect(image.width.toFloat() / image.height)
            },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sourceAspect) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)

                        var started = false
                        var pastSlop = false
                        var gestureZoom = currentZoom
                        var gestureFocalX = currentFocalX
                        var gestureFocalY = currentFocalY

                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.none { it.pressed }) break

                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastSlop) {
                                pastSlop = panChange.getDistance() > viewConfiguration.touchSlop ||
                                    abs(1f - zoomChange) > 0.01f
                                if (!pastSlop) continue
                            }

                            val aspect = sourceAspect
                            if (aspect == null) continue

                            if (!started) {
                                started = true
                                onGestureStart()
                            }

                            val targetAspect = size.width.toFloat() / size.height.toFloat()
                            val fractionX = min(1f, targetAspect / aspect)
                            val fractionY = min(1f, aspect / targetAspect)

                            val newZoom = (gestureZoom * zoomChange).coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM)
                            if (newZoom != gestureZoom) {
                                gestureZoom = newZoom
                                gestureFocalX = clampFocal(gestureFocalX, fractionX, gestureZoom)
                                gestureFocalY = clampFocal(gestureFocalY, fractionY, gestureZoom)
                                onZoom(gestureZoom)
                            }

                            if (panChange != Offset.Zero) {
                                gestureFocalX = clampFocal(
                                    gestureFocalX - panChange.x * fractionX / (size.width * gestureZoom),
                                    fractionX,
                                    gestureZoom,
                                )
                                gestureFocalY = clampFocal(
                                    gestureFocalY - panChange.y * fractionY / (size.height * gestureZoom),
                                    fractionY,
                                    gestureZoom,
                                )
                                onFocus(gestureFocalX, gestureFocalY)
                            }

                            event.changes.forEach { change ->
                                if (change.positionChanged()) change.consume()
                            }
                        }

                        if (started) onGestureEnd() else onTap()
                    }
                },
        )
    }
}

@Composable
private fun ThirdsGuides(
    modifier: Modifier = Modifier,
    alpha: Float,
) {
    if (alpha <= 0f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val color = Color.White.copy(alpha = 0.28f * alpha)
        val stroke = 1.dp.toPx()
        for (step in 1..2) {
            val x = size.width * step / 3f
            drawLine(color, Offset(x, 0f), Offset(x, size.height), stroke)
            val y = size.height * step / 3f
            drawLine(color, Offset(0f, y), Offset(size.width, y), stroke)
        }
    }
}

@Composable
private fun EditorFooter(
    modifier: Modifier = Modifier,
    uiState: WallpaperUiState,
    onZoomChange: (Float) -> Unit,
    onCenter: () -> Unit,
    onReset: () -> Unit,
) {
    val colors = MaterialTheme.wallmatic
    val centered = uiState.draftFocalX == 0.5f && uiState.draftFocalY == 0.5f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.paper)
            .navigationBarsPadding(),
    ) {
        HorizontalRule(color = colors.ink)

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "ZOOM",
                    style = WallmaticTextStyles.microStrong,
                    color = colors.inkMuted,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = zoomLabel(uiState.draftZoom),
                    style = WallmaticTextStyles.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.ink,
                )
            }

            ZoomRail(
                zoom = uiState.draftZoom,
                onZoomChange = onZoomChange,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextAction(
                    text = "Center",
                    enabled = !centered,
                    onClick = onCenter,
                )
                TextAction(
                    text = "Reset",
                    enabled = !centered || uiState.draftZoom != 1f,
                    onClick = onReset,
                )
            }
        }
    }
}

@Composable
private fun ZoomRail(
    modifier: Modifier = Modifier,
    zoom: Float,
    onZoomChange: (Float) -> Unit,
) {
    val colors = MaterialTheme.wallmatic
    val currentOnZoomChange by rememberUpdatedState(onZoomChange)
    val currentZoom by rememberUpdatedState(zoom)
    val thumbSize = 10.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .systemGestureExclusion()
            .pointerInput(Unit) {
                val thumbPx = thumbSize.toPx()
                val usableWidthPx = size.width - thumbPx

                fun zoomAt(x: Float): Float {
                    if (usableWidthPx <= 0f) return MIN_CROP_ZOOM
                    val fraction = ((x - thumbPx / 2) / usableWidthPx).coerceIn(0f, 1f)
                    return zoomForFraction(fraction)
                }

                var lastZoom = currentZoom

                fun emit(x: Float) {
                    val value = zoomAt(x)
                    if (value == lastZoom) return
                    lastZoom = value
                    currentOnZoomChange(value)
                }

                awaitEachGesture {
                    val down = awaitFirstDown()
                    lastZoom = currentZoom
                    emit(down.position.x)

                    drag(down.id) { change ->
                        change.consume()
                        emit(change.position.x)
                    }
                }
            },
    ) {
        val usableWidth = maxWidth - thumbSize

        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2
            drawLine(
                color = colors.ruleSoft,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 1.dp.toPx(),
            )
            ZoomTicks.indices.forEach { index ->
                val x = size.width * index / (ZoomTicks.size - 1)
                drawLine(
                    color = colors.ink,
                    start = Offset(x, centerY - 2.dp.toPx()),
                    end = Offset(x, centerY + 2.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = usableWidth * zoomFraction(zoom))
                .size(thumbSize)
                .background(colors.primary),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = "Zoom"
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = zoom,
                        range = MIN_CROP_ZOOM..MAX_CROP_ZOOM,
                        steps = 0,
                    )
                    setProgress { target ->
                        currentOnZoomChange(quantizeZoom(target))
                        true
                    }
                },
        )
    }
}

private val ZoomTicks = listOf(1f, 1.5f, 2f, 2.5f, 3f, 3.5f, 4f)

private fun zoomFraction(zoom: Float): Float = ((zoom - MIN_CROP_ZOOM) / (MAX_CROP_ZOOM - MIN_CROP_ZOOM))
    .coerceIn(0f, 1f)

private fun zoomForFraction(fraction: Float): Float =
    quantizeZoom(MIN_CROP_ZOOM + fraction * (MAX_CROP_ZOOM - MIN_CROP_ZOOM))

private fun quantizeZoom(zoom: Float): Float =
    (zoom.coerceIn(MIN_CROP_ZOOM, MAX_CROP_ZOOM) * 100f).roundToInt() / 100f

private data class CropGeometry(
    val fractionX: Float,
    val fractionY: Float,
)

private fun cropGeometry(sourceAspect: Float, targetAspect: Float): CropGeometry = CropGeometry(
    fractionX = min(1f, targetAspect / sourceAspect),
    fractionY = min(1f, sourceAspect / targetAspect),
)

private fun clampFocal(focal: Float, fraction: Float, zoom: Float): Float {
    val half = fraction / (2f * zoom)
    return focal.coerceIn(half, 1f - half)
}

private fun zoomLabel(zoom: Float): String {
    val rounded = (zoom * 10f).roundToInt() / 10f
    val text = if (rounded % 1f == 0f) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", rounded)
    }
    return "$text×"
}

private fun GraphicsLayerScope.applyCropTransform(
    sourceAspect: Float,
    zoom: Float,
    focalX: Float,
    focalY: Float,
) {
    if (sourceAspect <= 0f || size.width <= 0f || size.height <= 0f) return

    val targetAspect = size.width / size.height
    val sourceHeight = 1f / sourceAspect
    val cropWidth = min(1f, targetAspect / sourceAspect) / zoom
    val cropHeight = min(1f, sourceAspect / targetAspect) * sourceHeight / zoom
    if (cropWidth <= 0f || cropHeight <= 0f) return

    val left = (focalX - cropWidth / 2f).coerceIn(0f, 1f - cropWidth)
    val top = (focalY * sourceHeight - cropHeight / 2f).coerceIn(0f, sourceHeight - cropHeight)

    val fit = min(size.width, size.height / sourceHeight)
    val scale = size.width / cropWidth / fit
    val offsetX = (size.width - fit) / 2f
    val offsetY = (size.height - sourceHeight * fit) / 2f

    scaleX = scale
    scaleY = scale
    transformOrigin = TransformOrigin(0f, 0f)
    translationX = -left * (size.width / cropWidth) - scale * offsetX
    translationY = -top * (size.height / cropHeight) - scale * offsetY
    clip = true
}

@Composable
private fun Message(
    title: String,
    description: String? = null,
) {
    StateMessage(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.wallmatic.paper),
        title = title,
        description = description,
    )
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 915)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 915)
@Composable
private fun WallpaperViewPreview() {
    WallmaticTheme {
        Wallpaper(
            uiState = WallpaperUiState(
                isLoading = false,
                wallpaper = Wallpaper(uri = "").apply { id = 1 },
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 915)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 915)
@Composable
private fun WallpaperEditPreview() {
    WallmaticTheme {
        Wallpaper(
            uiState = WallpaperUiState(
                isLoading = false,
                wallpaper = Wallpaper(uri = "").apply { id = 1 },
                mode = WallpaperMode.Edit,
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 915)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 915)
@Composable
private fun WallpaperEditAdjustedPreview() {
    WallmaticTheme {
        Wallpaper(
            uiState = WallpaperUiState(
                isLoading = false,
                wallpaper = Wallpaper(
                    uri = "",
                    cropFocalX = 0.42f,
                    cropFocalY = 0.5f,
                    cropZoom = 2f,
                ).apply { id = 1 },
                mode = WallpaperMode.Edit,
                draftFocalX = 0.38f,
                draftFocalY = 0.5f,
                draftZoom = 2.5f,
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 412, heightDp = 915)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 412, heightDp = 915)
@Composable
private fun WallpaperMissingPreview() {
    WallmaticTheme {
        Wallpaper(uiState = WallpaperUiState(isLoading = false, isMissing = true), onEvent = {})
    }
}
