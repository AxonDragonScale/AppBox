package com.axondragonscale.limitless.ui.devtools

import com.axondragonscale.limitless.database.dao.GoalDao
import com.axondragonscale.limitless.database.dao.HabitDao
import com.axondragonscale.limitless.database.dao.RecordDao
import com.axondragonscale.limitless.database.dao.ReminderDao
import com.axondragonscale.limitless.database.entity.GoalEntity
import com.axondragonscale.limitless.database.entity.HabitEntity
import com.axondragonscale.limitless.database.entity.RecordEntity
import com.axondragonscale.limitless.database.entity.ReminderEntity
import com.axondragonscale.limitless.model.BitWeek
import com.axondragonscale.limitless.model.GoalType
import com.axondragonscale.limitless.model.HabitColor
import com.axondragonscale.limitless.model.ReminderType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.datetime.LocalTime

/**
 * Dev-only utility for seeding and clearing sample habits, records, goals, and reminders.
 * Operates directly on DAOs to avoid polluting the production repository.
 */
class SampleDataSeeder(
    private val habitDao: HabitDao,
    private val recordDao: RecordDao,
    private val reminderDao: ReminderDao,
    private val goalDao: GoalDao,
) {

    /**
     * Deletes all habits. Records, reminders, and goals are cascade-deleted via foreign keys.
     */
    suspend fun clearAll() {
        habitDao.clearAll()
    }

    /**
     * Seeds the database with sample habits, records, goals, and reminders for dev testing.
     *
     * Habits and their config:
     * - Meditation: daily goal, two daily reminders (morning + evening)
     * - Exercise:   weekly goal, weekly reminder on Mon/Wed/Fri
     * - Reading:    daily + monthly goals, daily reminder + one-time reminder next week
     * - Journaling: weekly goal, daily reminder + monthly reminder on the 1st
     *
     * This spread covers all [ReminderType]s and all [GoalType]s.
     *
     * Clears existing data before seeding.
     */
    suspend fun seed() {
        clearAll()

        seedMeditation()
        seedExercise()
        seedReading()
        seedJournaling()
    }

    private suspend fun seedMeditation() {
        val habit = insertHabit(
            name = "Meditation",
            description = "Daily mindfulness practice",
            frequency = 0.75f,
            maxPerDay = 2,
            color = HabitColor.TEAL,
        )

        // Goal: meditate at least once every day
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.DAILY, target = 1))

        // Reminders: morning and evening nudges
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(7, 0)))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(21, 0)))
    }

    private suspend fun seedExercise() {
        val habit = insertHabit(
            name = "Exercise",
            description = "Workout or physical activity",
            frequency = 0.45f,
            maxPerDay = 1,
            color = HabitColor.ORANGE,
        )

        // Goal: work out 4 times a week
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.WEEKLY, target = 4))

        // Reminder: early morning on Mon, Wed, Fri
        reminderDao.upsert(
            ReminderEntity(
                habitId = habit.id,
                type = ReminderType.WEEKLY,
                time = LocalTime(6, 30),
                daysOfWeek = BitWeek.of(1, 3, 5), // Mon, Wed, Fri
            )
        )
    }

    private suspend fun seedReading() {
        val habit = insertHabit(
            name = "Reading",
            description = "Read at least 20 pages",
            frequency = 0.55f,
            maxPerDay = 3,
            color = HabitColor.BLUE,
        )

        // Goals: read daily + hit 20 sessions a month
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.DAILY, target = 1))
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.MONTHLY, target = 20))

        // Reminder: every evening before bed
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(21, 30)))

        // Reminder: one-time reminder a week from now (covers ONE_TIME type in dev)
        val tz = TimeZone.currentSystemDefault()
        val nextWeek = Clock.System.now().toLocalDateTime(tz).date.plus(7, DateTimeUnit.DAY)
        reminderDao.upsert(
            ReminderEntity(
                habitId = habit.id,
                type = ReminderType.ONE_TIME,
                time = LocalTime(20, 0),
                date = nextWeek,
            )
        )
    }

    private suspend fun seedJournaling() {
        val habit = insertHabit(
            name = "Journaling",
            description = "Write daily reflections",
            frequency = 0.35f,
            maxPerDay = 1,
            color = HabitColor.PURPLE,
        )

        // Goal: journal 5 times a week
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.WEEKLY, target = 5))

        // Reminder: end-of-day prompt every night
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(22, 0)))

        // Reminder: monthly check-in on the 1st (covers MONTHLY type in dev)
        reminderDao.upsert(
            ReminderEntity(
                habitId = habit.id,
                type = ReminderType.MONTHLY,
                time = LocalTime(8, 0),
                dayOfMonth = 1,
            )
        )
    }

    /**
     * Inserts a [HabitEntity] and seeds records for the past ~6 months at the given [frequency].
     * Returns the saved entity with its auto-generated ID populated.
     */
    private suspend fun insertHabit(
        name: String,
        description: String,
        frequency: Float,
        maxPerDay: Int,
        color: HabitColor,
    ): HabitEntity {
        habitDao.upsert(HabitEntity(name = name, description = description, color = color))
        val habit = habitDao.getAllHabits().first { it.name == name }
        seedRecords(habit.id, frequency, maxPerDay)
        return habit
    }

    private suspend fun seedRecords(habitId: Long, frequency: Float, maxPerDay: Int) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val totalDays = 180 // ~6 months
        val random = Random(seed = habitId.toInt()) // Per-habit seed for varied but reproducible data

        for (day in 0 until totalDays) {
            if (random.nextFloat() > frequency) continue

            val recordCount = random.nextInt(1, maxPerDay + 1)
            repeat(recordCount) {
                val dayStart = now - (day * oneDayMs)
                val offsetMs = random.nextLong(0, oneDayMs)
                recordDao.upsert(
                    RecordEntity(
                        habitId = habitId,
                        timestamp = dayStart - oneDayMs + offsetMs,
                        message = "",
                    )
                )
            }
        }
    }
}
