package com.axondragonscale.logcatviewer.data

import com.axondragonscale.logcatviewer.model.SavedView
import kotlinx.coroutines.flow.Flow

/** Created by Ronak on 09/11/26 */

/** Named filter snapshots. `DataStoreSavedViewsStore` is the viewer's default implementation. */
interface ISavedViewsStore {
    val views: Flow<List<SavedView>>
    suspend fun save(view: SavedView)
    suspend fun rename(id: Long, name: String)
    suspend fun delete(id: Long)
}

/** The list transforms every store must apply: one entry per id, rename in place, delete by id. */
fun List<SavedView>.saving(view: SavedView): List<SavedView> = filterNot { it.id == view.id } + view

fun List<SavedView>.renaming(id: Long, name: String): List<SavedView> =
    map { if (it.id == id) it.copy(name = name) else it }

fun List<SavedView>.removing(id: Long): List<SavedView> = filterNot { it.id == id }
