package com.axondragonscale.commons.devtools.logs.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/** Created by Ronak Harkhani on 11/21/26 */
@Dao
internal interface DevLogDao {

    @Insert
    suspend fun insert(entries: List<DevLogEntity>)

    /**
     * Keyset paging on [beforeId]: records arriving mid-scroll cannot shift the window. The first
     * page passes [Long.MAX_VALUE].
     */
    @Query(
        """
        SELECT id, timestamp, priority, tag, message, session FROM logs
        WHERE id < :beforeId
          AND priority IN (:priorities)
          AND (:query = '' OR tag LIKE '%' || :query || '%' OR message LIKE '%' || :query || '%')
          AND (:includeTags = 0 OR tag IN (:tags))
          AND (:excludeTags = 0 OR tag NOT IN (:excludedTags))
        ORDER BY id DESC
        LIMIT :limit
        """
    )
    suspend fun page(
        beforeId: Long,
        priorities: List<String>,
        query: String,
        includeTags: Boolean,
        tags: List<String>,
        excludeTags: Boolean,
        excludedTags: List<String>,
        limit: Int,
    ): List<DevLogEntity>

    @Query("SELECT DISTINCT tag FROM logs ORDER BY tag")
    suspend fun tags(): List<String>

    /** Drops everything but the newest [keep] rows. Cheap when there is nothing to drop. */
    @Query("DELETE FROM logs WHERE id <= (SELECT MAX(id) FROM logs) - :keep")
    suspend fun trimToNewest(keep: Int)

    @Query("DELETE FROM logs")
    suspend fun clear()
}
