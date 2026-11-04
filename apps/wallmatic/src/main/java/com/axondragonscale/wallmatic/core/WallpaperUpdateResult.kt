package com.axondragonscale.wallmatic.core

/**
 * Outcome of a wallpaper update attempt.
 *
 * Callers must treat every variant except [Updated] as "the wallpaper may not have changed":
 * the updater only persists the parts of the config it actually applied, so a [Failed]
 * update leaves the cycle due and is the caller's signal to report or retry it.
 */
sealed interface WallpaperUpdateResult {

    /** At least one target screen was applied and persisted. */
    data object Updated : WallpaperUpdateResult

    /** Nothing applied: the update interval has not elapsed yet. */
    data object NotDue : WallpaperUpdateResult

    /** Nothing attempted: config missing/uninitialised, or the album(s) could not be loaded. */
    data class Skipped(val reason: String) : WallpaperUpdateResult

    /** Applying failed for at least one target. Persisted state only reflects targets that succeeded. */
    data class Failed(val cause: Throwable) : WallpaperUpdateResult
}
