package com.axondragonscale.commons

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Created by Ronak Harkhani on 25/01/25
 */

/**
 * Extension to collect a Flow without handling emissions.
 * Useful when you just want to trigger the flow collection.
 */
suspend fun <T> Flow<T>.collect() = this.collect {}

/**
 * Combines 6 flows into a single flow.
 * Returns a Flow that combines the most recently emitted values from each flow
 * via the provided transform function.
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

private data class Tuple5<T1, T2, T3, T4, T5>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5
)

data class Tuple6<T1, T2, T3, T4, T5, T6>(
    val t1: T1,
    val t2: T2,
    val t3: T3,
    val t4: T4,
    val t5: T5,
    val t6: T6
)
