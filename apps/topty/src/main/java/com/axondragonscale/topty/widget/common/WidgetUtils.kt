package com.axondragonscale.topty.widget.common

import com.axondragonscale.topty.model.Code
import com.axondragonscale.topty.model.HOTPCode
import com.axondragonscale.topty.model.TOTPCode

/**
 * Created by Ronak Harkhani on 29/12/24
 */

object WidgetUtils {

    /**
     * Format code with spaces (e.g., "123456" -> "123 456")
     */
    fun formatCode(code: String): String {
        return code.chunked(3).joinToString(" ")
    }

    /**
     * Calculate progress (0.0 to 1.0) for a code
     * - TOTP: Based on periodStart and periodEnd
     * - HOTP: Always 0.0 (no progress)
     */
    fun calculateProgress(code: Code): Float {
        return when (code) {
            is TOTPCode -> {
                val now = System.currentTimeMillis()
                val total = code.periodEnd - code.periodStart
                val elapsed = now - code.periodStart
                if (total > 0) {
                    (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
            is HOTPCode -> 0f
        }
    }

    /**
     * Calculate remaining seconds for a code
     * - TOTP: Based on periodEnd
     * - HOTP: Fixed 30 seconds or never expire (return Int.MAX_VALUE for no expiry)
     */
    fun calculateRemainingSeconds(code: Code): Int {
        return when (code) {
            is TOTPCode -> {
                val now = System.currentTimeMillis()
                val remaining = code.periodEnd - now
                (remaining / 1000).toInt().coerceAtLeast(0)
            }
            is HOTPCode -> 30 // Fixed 30 seconds for HOTP
        }
    }

    /**
     * Check if code has expired
     * - TOTP: Check against periodEnd
     * - HOTP: Never expires (or expires after fixed 30s if using fixed duration)
     */
    fun isCodeExpired(code: Code): Boolean {
        return when (code) {
            is TOTPCode -> {
                System.currentTimeMillis() >= code.periodEnd
            }
            is HOTPCode -> false // HOTP codes don't expire
        }
    }
}
