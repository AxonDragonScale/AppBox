package com.axondragonscale.limitless.repository

import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Created by Ronak Harkhani on 09/11/24
 */
class LimitlessRepository(
    private val habitDao: HabitDao,
    private val recordDao: RecordDao
) {

    fun getHabits(): Flow<List<HabitEntity>> = habitDao.getHabits()

    fun getRecords(): Flow<List<RecordEntity>> = recordDao.getRecords()

    fun getHabitsWithRecords(): Flow<Pair<List<HabitEntity>, List<RecordEntity>>> =
        combine(getHabits(), getRecords()) { habits, records -> Pair(habits, records) }

    suspend fun getHabit(id: Long) = habitDao.getHabit(id)

    suspend fun saveHabit(habit: HabitEntity) = habitDao.upsert(habit)

}
