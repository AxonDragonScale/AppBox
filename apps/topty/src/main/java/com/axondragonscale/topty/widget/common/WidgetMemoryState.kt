package com.axondragonscale.topty.widget.common

/**
 * Created by Ronak Harkhani on 29/12/24
 */

/**
 * In-memory storage for widget code visibility state.
 * This is intentionally not persisted - if process dies, codes are hidden for security.
 */
object WidgetMemoryState {
    private val expiryTimestamps = mutableMapOf<Int, Long>()

    /**
     * Set expiry timestamp for a widget (code will be visible until this time)
     */
    fun setExpiry(widgetId: Int, timestamp: Long) {
        expiryTimestamps[widgetId] = timestamp
    }

    /**
     * Get expiry timestamp for a widget
     * Returns 0 if not set or if code should be hidden
     */
    fun getExpiry(widgetId: Int): Long {
        val expiry = expiryTimestamps[widgetId] ?: 0
        val now = System.currentTimeMillis()

        // Auto-cleanup expired entries
        if (expiry > 0 && now >= expiry) {
            expiryTimestamps.remove(widgetId)
            return 0
        }

        return expiry
    }

    /**
     * Check if code should be visible for this widget
     */
    fun isCodeVisible(widgetId: Int): Boolean {
        return getExpiry(widgetId) > System.currentTimeMillis()
    }

    /**
     * Clear expiry for a widget (hide code)
     */
    fun clearExpiry(widgetId: Int) {
        expiryTimestamps.remove(widgetId)
    }

    /**
     * Clear all expiry timestamps (useful for cleanup)
     */
    fun clearAll() {
        expiryTimestamps.clear()
    }
}
