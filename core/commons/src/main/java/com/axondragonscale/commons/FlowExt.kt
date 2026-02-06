package com.axondragonscale.commons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

/**
 * Created by Ronak Harkhani on 25/01/25
 */

/**
 * Extension to collect a Flow without handling emissions.
 * Useful when you just want to trigger the flow collection.
 */
suspend fun <T> Flow<T>.collect() = this.collect {}

/**
 * Composable extension to collect a Flow with lifecycle awareness.
 * The flow collection starts when the lifecycle is STARTED and stops when it's STOPPED.
 * Emissions are handled on the Main.immediate dispatcher.
 *
 * @param onEffect Callback invoked for each flow emission
 */
@Composable
fun <T> Flow<T>.collectWithLifecycle(onEffect: (T) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(this, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                this@collectWithLifecycle.collect(onEffect)
            }
        }
    }
}

/**
 * Combines 6 flows into a single flow.
 * Returns a Flow that combines the most recently emitted values from each flow
 * via the provided transform function.
 *
 * @param flow1 First flow
 * @param flow2 Second flow
 * @param flow3 Third flow
 * @param flow4 Fourth flow
 * @param flow5 Fifth flow
 * @param flow6 Sixth flow
 * @param transform Function that combines the latest values from all flows
 * @return A Flow that emits the result of the transform function
 */
fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(
    combine(flow1, flow2, flow3, flow4, flow5, ::Tuple5),
    flow6
) { t5, t6 ->
    transform(t5.t1, t5.t2, t5.t3, t5.t4, t5.t5, t6)
}

/**
 * Combines 7 flows into a single flow.
 * Returns a Flow that combines the most recently emitted values from each flow
 * via the provided transform function.
 *
 * @param flow1 First flow
 * @param flow2 Second flow
 * @param flow3 Third flow
 * @param flow4 Fourth flow
 * @param flow5 Fifth flow
 * @param flow6 Sixth flow
 * @param flow7 Seventh flow
 * @param transform Function that combines the latest values from all flows
 * @return A Flow that emits the result of the transform function
 */
fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> = combine(
    combine(flow1, flow2, flow3, flow4, flow5, flow6, ::Tuple6),
    flow7
) { t6, t7 ->
    transform(t6.t1, t6.t2, t6.t3, t6.t4, t6.t5, t6.t6, t7)
}

/**
 * Internal data class to hold 5 values for combine implementation
 */
private data class Tuple5<T1, T2, T3, T4, T5>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5
)

/**
 * Internal data class to hold 6 values for combine implementation
 */
data class Tuple6<T1, T2, T3, T4, T5, T6>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6
)
