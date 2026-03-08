package com.axondragonscale.limitless.di

import com.axondragonscale.commons.room.createDatabase

import com.axondragonscale.limitless.database.LimitlessDatabase
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.day.HabitDayVM
import com.axondragonscale.limitless.ui.devtools.SampleDataSeeder
import com.axondragonscale.limitless.ui.editor.EditorVM
import com.axondragonscale.limitless.ui.habit.HabitVM
import com.axondragonscale.limitless.ui.habit.calendar.HabitCalendarVM
import com.axondragonscale.limitless.ui.habit.history.HistoryVM
import com.axondragonscale.limitless.ui.habit.stats.HabitStatsVM
import com.axondragonscale.limitless.ui.home.HomeVM
import com.axondragonscale.limitless.ui.settings.SettingsVM
import com.axondragonscale.limitless.ui.stats.StatsVM
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 01/11/24
 */

val LimitlessModule = module {

    // Database
    single { createDatabase<LimitlessDatabase>(LimitlessDatabase.DB_NAME) }
    single { get<LimitlessDatabase>().habitDao() }
    single { get<LimitlessDatabase>().recordDao() }
    single { get<LimitlessDatabase>().habitDayCountDao() }
    single { get<LimitlessDatabase>().habitHourCountDao() }
    single { get<LimitlessDatabase>().reminderDao() }
    single { get<LimitlessDatabase>().goalDao() }

    // Repository
    singleOf(::AppPrefsRepository)
    singleOf(::LimitlessRepository)

    // Dev Tools
    singleOf(::SampleDataSeeder)

    // ViewModel
    viewModelOf(::HomeVM)
    viewModelOf(::EditorVM)
    viewModelOf(::HabitVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::HabitCalendarVM)
    viewModelOf(::HabitDayVM)
    viewModelOf(::HabitStatsVM)
    viewModelOf(::StatsVM)
    viewModelOf(::SettingsVM)

}
