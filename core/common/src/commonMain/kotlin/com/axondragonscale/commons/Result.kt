package com.axondragonscale.commons

/**
 * Created by Ronak Harkhani on 27 January 2026.
A sealed interface representing the result of an operation that can either succeed or fail.
 *
 * This allows handling success and failure cases in a type-safe manner without using exceptions.
 * The interface uses two type parameters:
 * - S: The type of successful result
 * - F: The type of failure result
 *
 * The value accessors are members; the combinators are `inline` extensions declared below the
 * interface, so a lambda passed to them may use a non-local `return`.
 */
sealed interface Result<out S, out F> {

    data class Success<S>(val data: S) : Result<S, Nothing>
    data class Failure<F>(val data: F) : Result<Nothing, F>

    /**
     * Checks if this result represents a success.
     */
    fun isSuccess(): Boolean = this is Success<S>

    /**
     * Checks if this result represents a failure.
     */
    fun isFailure(): Boolean = this is Failure<F>

    /**
     * Gets the success data or null if this result is a failure.
     */
    fun getSuccessResultOrNull(): S? = (this as? Success)?.data

    /**
     * Gets the failure data or null if this result is a success.
     */
    fun getFailureResultOrNull(): F? = (this as? Failure)?.data

    /**
     * Gets the success data. Should only be called when you're sure this is a success.
     *
     * @return The success data
     * @throws ClassCastException if this result is not a [Success]
     */
    fun getSuccessResult(): S = (this as Success).data

    /**
     * Gets the failure data. Should only be called when you're sure this is a failure.
     *
     * @return The failure data
     * @throws ClassCastException if this result is not a [Failure]
     */
    fun getFailureResult(): F = (this as Failure).data
}

/**
 * Executes [block] with the success data and returns this result, so calls can be chained.
 */
inline fun <S, F> Result<S, F>.onSuccess(block: (S) -> Unit): Result<S, F> = when (this) {
    is Result.Success<S> -> {
        block(data)
        this
    }

    is Result.Failure<F> -> this
}

/**
 * Executes [block] with the failure data and returns this result, so calls can be chained.
 */
inline fun <S, F> Result<S, F>.onFailure(block: (F) -> Unit): Result<S, F> = when (this) {
    is Result.Success<S> -> this
    is Result.Failure<F> -> {
        block(data)
        this
    }
}

/**
 * Runs [block] whether this result is a success or a failure, then returns this result unchanged.
 */
inline fun <S, F> Result<S, F>.onComplete(block: () -> Unit): Result<S, F> {
    block()
    return this
}

/**
 * Collapses this result into a single value by applying one of the two cases.
 */
inline fun <S, F, R> Result<S, F>.fold(onSuccess: (S) -> R, onFailure: (F) -> R): R = when (this) {
    is Result.Success<S> -> onSuccess(data)
    is Result.Failure<F> -> onFailure(data)
}

/**
 * Transforms the success data, leaving a failure untouched.
 */
inline fun <S, F, R> Result<S, F>.map(transform: (S) -> R): Result<R, F> = when (this) {
    is Result.Success<S> -> Result.Success(transform(data))
    is Result.Failure<F> -> this
}

/**
 * Transforms the failure data, leaving a success untouched — e.g. turning a domain error into a
 * message to display, without touching the success path.
 */
inline fun <S, F, G> Result<S, F>.mapFailure(transform: (F) -> G): Result<S, G> = when (this) {
    is Result.Success<S> -> this
    is Result.Failure<F> -> Result.Failure(transform(data))
}

/**
 * Turns a failure into a success using [transform]; a success passes through unchanged.
 */
inline fun <S, F> Result<S, F>.recover(transform: (F) -> S): Result<S, Nothing> = when (this) {
    is Result.Success<S> -> this
    is Result.Failure<F> -> Result.Success(transform(data))
}

/**
 * Returns the success data, or [default] when this result is a failure.
 */
inline fun <S, F> Result<S, F>.getOrDefault(default: S): S = when (this) {
    is Result.Success<S> -> data
    is Result.Failure<F> -> default
}

/**
 * Returns the success data, or the value [onFailure] produces from the failure data.
 */
inline fun <S, F> Result<S, F>.getOrElse(onFailure: (F) -> S): S = when (this) {
    is Result.Success<S> -> data
    is Result.Failure<F> -> onFailure(data)
}

/**
 * Bridges a [kotlin.Result] into this type, converting the thrown exception with [onThrow].
 */
fun <S, F> kotlin.Result<S>.toResult(onThrow: (Throwable) -> F): Result<S, F> =
    fold({ Result.Success(it) }, { Result.Failure(onThrow(it)) })
