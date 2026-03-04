package com.axondragonscale.limitless.ui.devtools

import com.axondragonscale.commons.toEpochMillis
import com.axondragonscale.commons.today
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.random.Random

/**
 * Dev-only utility for seeding and clearing sample habits, records, goals, and reminders.
 * Operates directly on DAOs to avoid polluting the production repository.
 *
 * Seven habits are seeded, deliberately covering every permutation:
 *
 * | Habit        | Desc | Goals       | Reminders            | Pattern highlights                    |
 * |--------------|------|-------------|----------------------|---------------------------------------|
 * | Meditation   |  ✓   | 1 (daily)   | 2× DAILY             | Consistent, morning peak, long streak |
 * | Exercise     |  ✓   | 2 (wk+mo)   | 1× WEEKLY            | Mon/Wed/Fri, bimodal, growing trend   |
 * | Reading      |  ✓   | 3 (all)     | DAILY+MONTHLY+ONCE   | Evening, declining trend, all 3 goals |
 * | Journaling   |  ✓   | 1 (weekly)  | DAILY+MONTHLY        | Late night, sparse, broken streak     |
 * | Hydration    |  ✗   | 0           | 0                    | All-day, very consistent, long streak |
 * | Cold Shower  |  ✗   | 1 (daily)   | 1× DAILY             | Morning, recent 60 days only          |
 * | Stretching   |  ✓   | 2 (da+wk)   | 0                    | Bimodal, variable monthly pattern     |
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
     * Seeds the database with 7 sample habits covering all stat-tab permutations.
     * Clears existing data first.
     */
    suspend fun seed() {
        clearAll()
        seedMeditation()
        seedExercise()
        seedReading()
        seedJournaling()
        seedHydration()
        seedColdShower()
        seedStretching()
    }

    // -------------------------------------------------------------------------
    // Habit 1 — Meditation (TEAL)
    // Description  : yes
    // Goals        : 1 — DAILY target=1  (~78% met → high completion)
    // Reminders    : 2 — DAILY 7:00 AM + DAILY 9:00 PM
    // Record pattern: 180 days, ~78% frequency, all days even, morning peak 7–9 AM
    // Stats highlights: long current streak, morning time-of-day peak, steady monthly trend
    // -------------------------------------------------------------------------
    private suspend fun seedMeditation() {
        val habit = insertHabit(
            name = "Meditation",
            description = "Daily mindfulness and breathing practice",
            color = HabitColor.TEAL,
            totalDays = 360,
            random = Random(seed = 101),
            frequencyForDay = { _, _ -> 0.78f },
            maxPerDay = 1,
            pickHour = { r -> weighted(r, 6 to 1, 7 to 4, 8 to 3, 9 to 2) },
        )
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.DAILY, target = 1))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(7, 0)))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(21, 0)))
    }

    // -------------------------------------------------------------------------
    // Habit 2 — Exercise (ORANGE)
    // Description  : yes
    // Goals        : 2 — WEEKLY target=3, MONTHLY target=12
    // Reminders    : 1 — WEEKLY Mon/Wed/Fri/Sat 6:30 AM
    // Record pattern: 360 days, growing trend (sparse→dense), Mon/Wed/Fri dominant,
    //                 bimodal hours (early morning 6–8 AM OR late afternoon 17–19 PM)
    // Stats highlights: weekday-heavy weekly pattern, growing monthly bars, bimodal ToD chart
    // -------------------------------------------------------------------------
    private suspend fun seedExercise() {
        val habit = insertHabit(
            name = "Exercise",
            description = "Workout: run, lift, or any physical activity",
            color = HabitColor.ORANGE,
            totalDays = 360,
            random = Random(seed = 202),
            frequencyForDay = { daysAgo, date ->
                // Frequency grows toward today: 0.30 six months ago → 0.80 today
                val trend = 0.30f + 0.50f * (1f - daysAgo / 360f)
                when (date.dayOfWeek.isoDayNumber) {
                    1, 3, 5 -> trend             // Mon / Wed / Fri — primary days
                    6       -> trend * 0.55f     // Sat — occasional
                    else    -> trend * 0.12f     // Tue / Thu / Sun — rare
                }
            },
            maxPerDay = 1,
            pickHour = { r -> if (r.nextBoolean()) r.nextInt(6, 9) else r.nextInt(17, 20) },
        )
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.WEEKLY, target = 3))
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.MONTHLY, target = 12))
        reminderDao.upsert(
            ReminderEntity(
                habitId = habit.id,
                type = ReminderType.WEEKLY,
                time = LocalTime(6, 30),
                daysOfWeek = BitWeek.of(1, 3, 5, 6), // Mon, Wed, Fri, Sat
            )
        )
    }

    // -------------------------------------------------------------------------
    // Habit 3 — Reading (BLUE)
    // Description  : yes
    // Goals        : 3 — DAILY target=1, WEEKLY target=5, MONTHLY target=20  (all 3 types)
    // Reminders    : 3 — DAILY 21:30, MONTHLY 1st 8:00, ONE_TIME next week 20:00
    // Record pattern: 360 days, declining trend (dense→sparse), evening peak 20–22,
    //                 slight weekend lean, up to 2 records/day
    // Stats highlights: all 3 goal rings, declining monthly bars, evening ToD peak
    // -------------------------------------------------------------------------
    private suspend fun seedReading() {
        val nextWeek = LocalDate.today().plus(7, DateTimeUnit.DAY)

        val habit = insertHabit(
            name = "Reading",
            description = "Read at least 20 pages of a book",
            color = HabitColor.BLUE,
            totalDays = 360,
            random = Random(seed = 303),
            frequencyForDay = { daysAgo, date ->
                // Frequency declines toward today: ~0.85 six months ago → ~0.35 today
                val trend = 0.85f - 0.50f * (1f - daysAgo / 360f)
                val weekendBonus = if (date.dayOfWeek.isoDayNumber in 6..7) 0.15f else 0f
                (trend + weekendBonus).coerceIn(0.05f, 0.95f)
            },
            maxPerDay = 2,
            pickHour = { r -> weighted(r, 19 to 1, 20 to 3, 21 to 4, 22 to 2) },
        )
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.DAILY, target = 1))
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.WEEKLY, target = 5))
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.MONTHLY, target = 20))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(21, 30)))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.MONTHLY, time = LocalTime(8, 0), dayOfMonth = 1))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.ONE_TIME, time = LocalTime(20, 0), date = nextWeek))
    }

    // -------------------------------------------------------------------------
    // Habit 4 — Journaling (PURPLE)
    // Description  : yes
    // Goals        : 1 — WEEKLY target=5  (~40% met → low completion)
    // Reminders    : 2 — DAILY 22:00 + MONTHLY 1st 08:00
    // Record pattern: 90 days only, sparse ~38%, ZERO records in last 3 days (broken streak),
    //                 late-night peak 22–23
    // Stats highlights: broken current streak (= 0), short 3-month history, late-night ToD
    // -------------------------------------------------------------------------
    private suspend fun seedJournaling() {
        val habit = insertHabit(
            name = "Journaling",
            description = "Evening reflections and gratitude notes",
            color = HabitColor.PURPLE,
            totalDays = 90,
            random = Random(seed = 404),
            frequencyForDay = { daysAgo, _ ->
                if (daysAgo < 3) 0f else 0.38f  // 3-day gap → currentStreak = 0
            },
            maxPerDay = 1,
            pickHour = { r -> weighted(r, 21 to 1, 22 to 4, 23 to 3) },
        )
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.WEEKLY, target = 5))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(22, 0)))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.MONTHLY, time = LocalTime(8, 0), dayOfMonth = 1))
    }

    // -------------------------------------------------------------------------
    // Habit 5 — Hydration (CYAN)
    // Description  : none  ← no-description case
    // Goals        : 0     ← zero-goals case
    // Reminders    : 0     ← zero-reminders case
    // Record pattern: 360 days, very consistent ~97%, 2–6 records/day, all-day spread 7–21
    // Stats highlights: very long current streak, huge total records, flat even ToD histogram,
    //                   consistent weekly pattern across all 7 days
    // -------------------------------------------------------------------------
    private suspend fun seedHydration() {
        insertHabit(
            name = "Hydration",
            description = "",
            color = HabitColor.CYAN,
            totalDays = 360,
            random = Random(seed = 505),
            frequencyForDay = { _, _ -> 0.97f },
            maxPerDay = 6,
            pickHour = { r -> r.nextInt(7, 22) },
        )
        // No goals, no reminders — intentional
    }

    // -------------------------------------------------------------------------
    // Habit 6 — Cold Shower (INDIGO)
    // Description  : none  ← second no-description case
    // Goals        : 1 — DAILY target=1  (~90% met → very high completion)
    // Reminders    : 1 — DAILY 6:45 AM
    // Record pattern: 60 days only (recently started), ~90% frequency, morning peak 6–8 AM
    // Stats highlights: strong current streak, only 2 months of monthly bars, 1 goal ring,
    //                   very narrow morning-only ToD chart
    // -------------------------------------------------------------------------
    private suspend fun seedColdShower() {
        val habit = insertHabit(
            name = "Cold Shower",
            description = "",
            color = HabitColor.INDIGO,
            totalDays = 60,
            random = Random(seed = 606),
            frequencyForDay = { _, _ -> 0.90f },
            maxPerDay = 1,
            pickHour = { r -> weighted(r, 6 to 3, 7 to 5, 8 to 2) },
        )
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.DAILY, target = 1))
        reminderDao.upsert(ReminderEntity(habitId = habit.id, type = ReminderType.DAILY, time = LocalTime(6, 45)))
    }

    // -------------------------------------------------------------------------
    // Habit 7 — Stretching (LIME)
    // Description  : yes
    // Goals        : 2 — DAILY target=1, WEEKLY target=4
    // Reminders    : 0   ← zero-reminders with goals
    // Record pattern: 360 days, variable monthly frequency (cycles: active→moderate→lazy),
    //                 bimodal hours (morning 6–8 AM OR evening 18–20 PM), up to 2/day
    // Stats highlights: variable monthly bars (peaks and troughs), bimodal ToD chart,
    //                   no reminders configured
    // -------------------------------------------------------------------------
    private suspend fun seedStretching() {
        val habit = insertHabit(
            name = "Stretching",
            description = "Morning or evening stretching routine",
            color = HabitColor.LIME,
            totalDays = 360,
            random = Random(seed = 707),
            frequencyForDay = { daysAgo, _ ->
                // Cycles every ~30 days: active (70%) → moderate (35%) → lazy (12%)
                when ((daysAgo / 30) % 3) {
                    0    -> 0.70f
                    1    -> 0.35f
                    else -> 0.12f
                }
            },
            maxPerDay = 2,
            pickHour = { r ->
                // 1-in-3 chance of morning session, otherwise evening
                if (r.nextInt(3) == 0) r.nextInt(6, 9) else r.nextInt(18, 21)
            },
        )
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.DAILY, target = 1))
        goalDao.upsert(GoalEntity(habitId = habit.id, type = GoalType.WEEKLY, target = 4))
        // No reminders — intentional
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Inserts a [HabitEntity] then seeds records for it according to the provided pattern.
     * Returns the saved entity with its auto-generated ID populated.
     */
    private suspend fun insertHabit(
        name: String,
        description: String,
        color: HabitColor,
        totalDays: Int,
        random: Random,
        /**
         * Returns the probability (0f..1f) of at least one record on a given day.
         * [daysAgo] is 0 for today, [totalDays]-1 for the oldest day.
         * [date] is the concrete [LocalDate] for that day, useful for day-of-week logic.
         */
        frequencyForDay: (daysAgo: Int, date: LocalDate) -> Float,
        /** Maximum number of records on an active day. Actual count is random in 1..maxPerDay. */
        maxPerDay: Int,
        /** Returns the hour (0–23) for a single record timestamp. */
        pickHour: (Random) -> Int,
    ): HabitEntity {
        habitDao.upsert(HabitEntity(name = name, description = description, color = color))
        val habit = habitDao.getAllHabits().first { it.name == name }
        seedRecords(habit.id, totalDays, random, frequencyForDay, maxPerDay, pickHour)
        return habit
    }

    private suspend fun seedRecords(
        habitId: Long,
        totalDays: Int,
        random: Random,
        frequencyForDay: (daysAgo: Int, date: LocalDate) -> Float,
        maxPerDay: Int,
        pickHour: (Random) -> Int,
    ) {
        val today = LocalDate.today()

        for (daysAgo in 0 until totalDays) {
            val date = today.minus(daysAgo, DateTimeUnit.DAY)
            if (random.nextFloat() > frequencyForDay(daysAgo, date)) continue

            val count = if (maxPerDay <= 1) 1 else random.nextInt(1, maxPerDay + 1)
            repeat(count) {
                val hour = pickHour(random)
                val minute = random.nextInt(0, 60)
                val timestamp = LocalDateTime(date, LocalTime(hour, minute)).toEpochMillis()
                recordDao.upsert(RecordEntity(habitId = habitId, timestamp = timestamp, message = ""))
            }
        }
    }

    /**
     * Picks a random hour from a weighted distribution.
     *
     * Each [weights] pair is `(hour, relativeWeight)`. The hour is selected proportionally:
     * e.g. `weighted(r, 7 to 4, 8 to 3, 9 to 2)` returns 7 with probability 4/9, 8 with 3/9, etc.
     */
    private fun weighted(random: Random, vararg weights: Pair<Int, Int>): Int {
        val total = weights.sumOf { it.second }
        var remaining = random.nextInt(total)
        for ((hour, weight) in weights) {
            remaining -= weight
            if (remaining < 0) return hour
        }
        return weights.last().first
    }
}
