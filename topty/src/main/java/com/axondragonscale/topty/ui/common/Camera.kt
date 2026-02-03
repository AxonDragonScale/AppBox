package com.axondragonscale.topty.ui.common

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/**
 * Created by Ronak Harkhani on 12/10/24
 */

@OptIn(ExperimentalGetImage::class)
@Composable
fun Camera(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean,
    analyzer: ImageAnalysis.Analyzer
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setImageAnalysisAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
            bindToLifecycle(lifecycleOwner)
        }
    }

    LaunchedEffect(flashEnabled) {
        cameraController.cameraControl?.enableTorch(flashEnabled)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                controller = cameraController
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }
    )
}
