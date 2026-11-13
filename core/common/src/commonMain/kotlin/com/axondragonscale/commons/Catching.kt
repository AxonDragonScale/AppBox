package com.axondragonscale.commons

import kotlinx.coroutines.CancellationException

/**
 * Created by Ronak Harkhani on 11/14/26
 */

/**
 * Runs [block], logging any [Exception] under this receiver's tag and returning it as a typed
 * [Result]. Use a `String` receiver for an explicit tag, or call it inside a class to use the
 * class name:
 *
 * ```
 * val result = catching { readConfig() }
 * val tagged = "Sync".catching { syncAlbums() }
 * ```
 *
 * Cancellation is rethrown so a cancelled coroutine is never reported as a failure; [Error]s
 * propagate unchanged. Chain [onFailure] for extra handling and [onComplete] for result cleanup.
 * Use [onFinally] for work that must run on every path, including cancellation and [Error].
 */
inline fun <T> Any.catching(
    crossinline onFinally: () -> Unit = {},
    block: () -> T,
): Result<T, Exception> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        this.log { e }
        Result.Failure(e)
    } finally {
        onFinally()
    }

/**
 * The cleanup sink for [catchingFinally]. Register work with [onFinally]; it runs after the block
 * on success, failure, cancellation and [Error], exactly like a `finally` block.
 */
class CatchingScope {

    private val cleanups = mutableListOf<() -> Unit>()

    fun onFinally(block: () -> Unit) {
        cleanups += block
    }

    @PublishedApi
    internal fun runFinally() {
        cleanups.forEach { it() }
    }
}

/**
 * Like [catching], but the block can register cleanup with [CatchingScope.onFinally]. The cleanup
 * runs on every path, so a resource created inside the block can be released without a local
 * `try/finally`:
 *
 * ```
 * catchingFinally { scope ->
 *     val bitmap = decode()
 *     scope.onFinally { bitmap.recycle() }
 *     apply(bitmap)
 * }
 * ```
 */
inline fun <T> Any.catchingFinally(block: (CatchingScope) -> T): Result<T, Exception> {
    val scope = CatchingScope()
    return catching(onFinally = { scope.runFinally() }) { block(scope) }
}
