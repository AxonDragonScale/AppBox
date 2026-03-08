package com.axondragonscale.limitless.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.axondragonscale.limitless.database.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ronak Harkhani on 02/11/24
 */
@Dao
interface RecordDao {

    // MARK: Getters -------------------------------------------------------------------------------

    @Query("SELECT * FROM record WHERE habitId = :habitId")
    fun getRecords(habitId: Long): Flow<List<RecordEntity>>

    @Query("SELECT * FROM record WHERE habitId = :habitId")
    suspend fun getAllRecords(habitId: Long): List<RecordEntity>

    @Query("SELECT * FROM record")
    fun getRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM record")
    suspend fun getAllRecords(): List<RecordEntity>

    @Query("SELECT * FROM record WHERE id = :id")
    suspend fun getRecord(id: Long): RecordEntity?

    @Query("""
        SELECT * FROM record
        WHERE habitId = :habitId
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getRecordsPaginated(habitId: Long, limit: Int, offset: Int): List<RecordEntity>

    @Query("SELECT COUNT(*) FROM record WHERE habitId = :habitId")
    suspend fun getRecordCount(habitId: Long): Int

    @Query("SELECT COUNT(*) FROM record WHERE habitId = :habitId")
    fun getRecordCountFlow(habitId: Long): Flow<Int>

    @Query("SELECT MIN(timestamp) FROM record WHERE habitId = :habitId")
    fun getFirstRecordTimestamp(habitId: Long): Flow<Long?>

    @Query("SELECT COUNT(DISTINCT date) FROM habit_day_count_view WHERE habitId = :habitId")
    fun getActiveDayCount(habitId: Long): Flow<Int>

    @Query("""
        SELECT * FROM record
        WHERE habitId = :habitId
        AND DATE(timestamp / 1000, 'unixepoch', 'localtime') = :date
        ORDER BY timestamp DESC
        LIMIT 1
    """)
    suspend fun getMostRecentRecordForDate(habitId: Long, date: String): RecordEntity?

    @Query("""
        SELECT * FROM record
        WHERE habitId = :habitId
        AND DATE(timestamp / 1000, 'unixepoch', 'localtime') = :date
        ORDER BY timestamp DESC
    """)
    fun getRecordsForDate(habitId: Long, date: String): Flow<List<RecordEntity>>

    // MARK: Upserts -------------------------------------------------------------------------------

    @Upsert
    suspend fun upsert(record: RecordEntity)

    // MARK: Delete --------------------------------------------------------------------------------

    @Query("DELETE FROM record WHERE id = :id")
    suspend fun delete(id: Long)

    @Delete
    suspend fun delete(record: RecordEntity)

    @Query("DELETE FROM record")
    suspend fun clearAll()

}
