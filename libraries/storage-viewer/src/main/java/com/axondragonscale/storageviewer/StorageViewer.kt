package com.axondragonscale.storageviewer

import android.content.ClipData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.axondragonscale.storageviewer.data.AndroidStorageSource
import com.axondragonscale.storageviewer.data.IStorageSource
import com.axondragonscale.storageviewer.ui.StorageEffect
import com.axondragonscale.storageviewer.ui.StorageEvent
import com.axondragonscale.storageviewer.ui.StorageScreen
import com.axondragonscale.storageviewer.ui.StorageViewModel
import com.axondragonscale.storageviewer.ui.style.LocalStorageViewerStyle
import com.axondragonscale.storageviewer.ui.style.StorageViewerStyle
import com.axondragonscale.storageviewer.ui.style.rememberStorageViewerStyle

/** Created by Ronak on 14/11/26 */

/**
 * A read-only view of the host app's own storage: databases, DataStore/SharedPreferences stores,
 * then the five files roots. Reads run against the live files; nothing is copied, exported or
 * written.
 *
 * The default [source] reads this app through [LocalContext]; a host that wants a different scope
 * passes its own. Copy runs immediately and the platform's own confirmation is the only feedback.
 *
 * It renders in the ambient `MaterialTheme`, so it must be hosted inside the DevTools module's
 * `DevToolsTheme` — the library supplies no theme of its own.
 *
 * Design source: `libraries/storage-viewer/storage-viewer.fig`, screens 01-06.
 */
@Composable
public fun StorageViewer(
    modifier: Modifier = Modifier,
    source: IStorageSource? = null,
    title: String = "Storage",
    style: StorageViewerStyle = rememberStorageViewerStyle(),
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val resolvedSource = source ?: remember(context) { AndroidStorageSource(context) }
    val viewModel: StorageViewModel = viewModel(factory = StorageViewModel.factory(resolvedSource))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current

    LaunchedEffect(Unit) { viewModel.onEvent(StorageEvent.Visible) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StorageEffect.Copy -> clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText(effect.label, effect.text)),
                )

                is StorageEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    CompositionLocalProvider(LocalStorageViewerStyle provides style) {
        StorageScreen(
            modifier = modifier,
            state = state,
            title = title,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onEvent = viewModel::onEvent,
        )
    }
}
