package com.axondragonscale.limitless.repository

import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.entity.HabitEntity

/**
 * Created by Ronak Harkhani on 09/11/24
 */
class LimitlessRepository(
    private val habitDao: HabitDao,
    private val recordDao: RecordDao
) {

    suspend fun getHabit(id: Long) = habitDao.getHabit(id)

    suspend fun saveHabit(habit: HabitEntity) = habitDao.upsert(habit)

}
