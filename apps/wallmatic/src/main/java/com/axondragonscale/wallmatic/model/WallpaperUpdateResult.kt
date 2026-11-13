package com.axondragonscale.wallmatic.model

/**
 * Outcome of a wallpaper update attempt.
 *
 * A [Failed] with nothing applied leaves the cycle due: `lastUpdated` does not advance. A partial
 * apply does advance `lastUpdated` and relies on the recorded failure for the retry. [Skipped] also
 * covers an album whose every wallpaper is blacklisted.
 */
sealed interface WallpaperUpdateResult {

    /** At least one target screen was applied and persisted. */
    data object Updated : WallpaperUpdateResult

    /** Nothing applied: the update interval has not elapsed yet. */
    data object NotDue : WallpaperUpdateResult

    /** Nothing applied: config missing/uninitialised, album missing, or every wallpaper blacklisted. */
    data class Skipped(val reason: String) : WallpaperUpdateResult

    /** Applying failed for at least one target. Persisted state only reflects targets that succeeded. */
    data class Failed(val cause: Exception) : WallpaperUpdateResult
}
