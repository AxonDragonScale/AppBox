package com.axondragonscale.commons

/**
 * Created by Ronak Harkhani on 27 January 2026.
A sealed interface representing the result of an operation that can either succeed or fail.
 *
 * This allows handling success and failure cases in a type-safe manner without using exceptions.
 * The interface uses two type parameters:
 * - S: The type of successful result
 * - F: The type of failure result
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

    /**
     * Executes the given [block] if this is a [Success].
     *
     * @param block The function to execute with the success data
     * @return This result, to allow for chaining
     */
    fun onSuccess(block: (S) -> Unit) = this.apply { if (this is Success) block.invoke(this.data) }

    /**
     * Executes the given [block] if this is a [Failure].
     *
     * @param block The function to execute with the failure data
     * @return This result, to allow for chaining
     */
    fun onFailure(block: (F) -> Unit) = this.apply { if (this is Failure) block.invoke(this.data) }

}
