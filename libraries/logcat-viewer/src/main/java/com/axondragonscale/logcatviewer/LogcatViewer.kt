package com.axondragonscale.logcatviewer

import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axondragonscale.logcatviewer.data.DataStoreSavedViewsStore
import com.axondragonscale.logcatviewer.data.ILogcatSource
import com.axondragonscale.logcatviewer.data.ISavedViewsStore
import com.axondragonscale.logcatviewer.data.LogcatPackageResolver
import com.axondragonscale.logcatviewer.data.ProcessLogcatSource
import com.axondragonscale.logcatviewer.ui.LogcatScreen
import com.axondragonscale.logcatviewer.ui.LogcatUiEffect
import com.axondragonscale.logcatviewer.ui.LogcatUiEvent
import com.axondragonscale.logcatviewer.ui.LogcatViewModel
import com.axondragonscale.logcatviewer.ui.style.LocalLogcatStyle
import com.axondragonscale.logcatviewer.ui.style.LogcatStyle
import com.axondragonscale.logcatviewer.ui.style.rememberLogcatStyle

/** Created by Ronak on 09/11/26 */

/**
 * A log viewer a host app can drop into a debug build.
 *
 * It reads the lines `logcat` will give this app, which is the app's own output — no permission and
 * no helper app are involved. Saved views persist in the library's own DataStore; a host can pass a
 * different [ISavedViewsStore], and a host that can reach more than its own logs passes its own
 * [ILogcatSource].
 *
 * Design source: `Logcat · Console board` in Devinator's `devinator.fig`.
 */
@Composable
fun LogcatViewer(
    modifier: Modifier = Modifier,
    source: ILogcatSource = remember { ProcessLogcatSource() },
    savedViewsStore: ISavedViewsStore? = null,
    title: String = "Logcat",
    style: LogcatStyle = rememberLogcatStyle(),
    onBack: (() -> Unit)? = null,
    onShare: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val store = savedViewsStore ?: remember(context) { DataStoreSavedViewsStore(context) }
    val packageResolver = remember(context) { LogcatPackageResolver(context) }
    val viewModel: LogcatViewModel = viewModel(
        factory = LogcatViewModel.factory(source, store, packageResolver),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val share = remember(context, onShare) { onShare ?: { text: String -> shareLog(context, text) } }
    val currentShare by rememberUpdatedState(share)

    // The reader follows the screen's visibility. Backgrounding closes the child, which the platform
    // stops delivering to anyway; returning reopens it and fills the gap from the buffer.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onEvent(LogcatUiEvent.Start) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onEvent(LogcatUiEvent.Stop) }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LogcatUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is LogcatUiEffect.Share -> currentShare(effect.text)
            }
        }
    }

    CompositionLocalProvider(LocalLogcatStyle provides style) {
        LogcatScreen(
            modifier = modifier,
            uiState = uiState,
            title = title,
            snackbarHostState = snackbarHostState,
            onEvent = viewModel::onEvent,
            onBack = onBack,
        )
    }
}

private fun shareLog(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching { context.startActivity(Intent.createChooser(send, null)) }
}
