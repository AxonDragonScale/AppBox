package com.axondragonscale.limitless.ui.devtools

import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import kotlin.random.Random

/**
 * Dev-only utility for seeding and clearing sample habits and records.
 * Operates directly on DAOs to avoid polluting the production repository.
 */
class SampleDataSeeder(
    private val habitDao: HabitDao,
    private val recordDao: RecordDao,
) {

    /**
     * Deletes all habits. Records are cascade-deleted via the foreign key.
     */
    suspend fun clearAll() {
        habitDao.clearAll()
    }

    /**
     * Seeds the database with sample habits and records for dev testing.
     * Creates 4 habits with records scattered across the last ~6 months
     * with varying densities to produce a realistic heatmap.
     *
     * Clears existing data before seeding.
     */
    suspend fun seed() {
        clearAll()

        val seedHabits = listOf(
            SeedHabit("Meditation", "Daily mindfulness practice", frequency = 0.75f, maxPerDay = 2),
            SeedHabit("Exercise", "Workout or physical activity", frequency = 0.45f, maxPerDay = 1),
            SeedHabit("Reading", "Read at least 20 pages", frequency = 0.55f, maxPerDay = 3),
            SeedHabit("Journaling", "Write daily reflections", frequency = 0.35f, maxPerDay = 1),
        )

        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val totalDays = 180 // ~6 months
        val random = Random(seed = 42) // Fixed seed for reproducible data

        for (seedHabit in seedHabits) {
            val habit = HabitEntity(
                name = seedHabit.name,
                description = seedHabit.description,
            )
            habitDao.upsert(habit)

            // Re-fetch to get the auto-generated ID
            val savedHabit = habitDao.getAllHabits().first { it.name == seedHabit.name }

            for (day in 0 until totalDays) {
                if (random.nextFloat() > seedHabit.frequency) continue

                val recordCount = random.nextInt(1, seedHabit.maxPerDay + 1)
                repeat(recordCount) {
                    // Spread records within the day with a random offset
                    val dayStart = now - (day * oneDayMs)
                    val offsetMs = random.nextLong(0, oneDayMs)
                    val timestamp = dayStart - oneDayMs + offsetMs

                    recordDao.upsert(
                        RecordEntity(
                            habitId = savedHabit.id,
                            timestamp = timestamp,
                            message = "",
                        )
                    )
                }
            }
        }
    }

    private data class SeedHabit(
        val name: String,
        val description: String,
        val frequency: Float,
        val maxPerDay: Int,
    )
}
