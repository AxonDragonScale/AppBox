package com.axondragonscale.storageviewer.model

/** Created by Ronak on 14/11/26 */

/** [Busy] is a database a writer holds; [Unreadable] is everything else that failed to open. */
public sealed interface StorageError {

    public data object Busy : StorageError

    public data class Unreadable(val detail: String?) : StorageError
}
