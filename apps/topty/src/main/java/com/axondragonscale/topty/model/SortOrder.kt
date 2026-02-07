package com.axondragonscale.topty.model

/**
 * Enum representing different sorting options for token list.
 *
 * Created by Ronak Harkhani on 26/01/25
 */
enum class SortOrder {
    /** Default database insertion order (oldest first) */
    ADDED_FIRST,

    /** Reverse database insertion order (newest first) */
    ADDED_LAST,

    /** Alphabetically by name (A-Z) */
    NAME_ASC,

    /** Reverse alphabetically by name (Z-A) */
    NAME_DESC,

    /** Alphabetically by issuer (A-Z) */
    ISSUER_ASC,

    /** Reverse alphabetically by issuer (Z-A) */
    ISSUER_DESC,

    /** Alphabetically by group (A-Z) */
    GROUP_ASC,

    /** Reverse alphabetically by group (Z-A) */
    GROUP_DESC,

    /** Most recently used first */
    RECENTLY_USED;

    companion object {
        val DEFAULT = ADDED_FIRST
    }

    /**
     * Returns a user-friendly display name for the sort order.
     */
    fun displayName(): String = when (this) {
        ADDED_FIRST -> "Added First"
        ADDED_LAST -> "Added Last"
        NAME_ASC -> "Name (A-Z)"
        NAME_DESC -> "Name (Z-A)"
        ISSUER_ASC -> "Issuer (A-Z)"
        ISSUER_DESC -> "Issuer (Z-A)"
        GROUP_ASC -> "Group (A-Z)"
        GROUP_DESC -> "Group (Z-A)"
        RECENTLY_USED -> "Recently Used"
    }
}
