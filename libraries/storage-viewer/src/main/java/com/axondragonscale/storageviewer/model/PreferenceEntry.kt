package com.axondragonscale.storageviewer.model

/** Created by Ronak on 14/11/26 */

public enum class PreferenceType { STRING, INT, LONG, FLOAT, DOUBLE, BOOL, STRING_SET, BYTES }

/** [display] is the value as the row and the value sheet show it, already capped when needed. */
public data class PreferenceEntry(
    val key: String,
    val type: PreferenceType,
    val display: String,
    val byteCount: Int,
    val truncated: Boolean,
)
