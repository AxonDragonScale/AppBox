package com.axondragonscale.commons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Composable extension to collect a Flow with lifecycle awareness.
 * The flow collection starts when the lifecycle is STARTED and stops when it's STOPPED.
 * Emissions are handled on the Main.immediate dispatcher.
 *
 * @param onEffect Callback invoked for each flow emission
 */
@Composable
fun <T> Flow<T>.collectWithLifecycle(
    context: CoroutineContext = Dispatchers.Main.immediate,
    onEffect: suspend (T) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(this, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(context) {
                this@collectWithLifecycle.collect(onEffect)
            }
        }
    }
}
