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
 * propagate unchanged. Chain [onFailure] for extra handling and [onComplete] for cleanup.
 */
inline fun <T> Any.catching(block: () -> T): Result<T, Exception> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        this.log { e }
        Result.Failure(e)
    }
