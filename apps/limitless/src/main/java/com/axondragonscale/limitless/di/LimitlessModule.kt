package com.axondragonscale.limitless.di

import com.axondragonscale.limitless.core.ReminderNotificationManager
import com.axondragonscale.limitless.core.ReminderScheduler
import com.axondragonscale.limitless.database.LimitlessDatabase
import com.axondragonscale.limitless.repository.AppPrefsRepository
import com.axondragonscale.limitless.repository.LimitlessRepository
import com.axondragonscale.limitless.ui.devtools.SampleDataSeeder
import com.axondragonscale.limitless.ui.editor.EditorVM
import com.axondragonscale.limitless.ui.habit.HabitVM
import com.axondragonscale.limitless.ui.habit.calendar.HabitCalendarVM
import com.axondragonscale.limitless.ui.day.HabitDayVM
import com.axondragonscale.limitless.ui.habit.history.HistoryVM
import com.axondragonscale.limitless.ui.home.HomeVM
import com.axondragonscale.limitless.ui.settings.SettingsVM
import com.axondragonscale.limitless.ui.habit.stats.StatsVM
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 01/11/24
 */

val LimitlessModule = module {

    // Database
    singleOf(LimitlessDatabase::create)
    singleOf(LimitlessDatabase::habitDao)
    singleOf(LimitlessDatabase::recordDao)
    singleOf(LimitlessDatabase::habitDayCountDao)
    singleOf(LimitlessDatabase::habitHourCountDao)
    singleOf(LimitlessDatabase::reminderDao)
    singleOf(LimitlessDatabase::goalDao)

    // Repository
    singleOf(::AppPrefsRepository)
    singleOf(::LimitlessRepository)

    // Core
    singleOf(::ReminderScheduler)
    singleOf(::ReminderNotificationManager)

    // Dev Tools
    singleOf(::SampleDataSeeder)

    // ViewModel
    viewModelOf(::HomeVM)
    viewModelOf(::EditorVM)
    viewModelOf(::HabitVM)
    viewModelOf(::HistoryVM)
    viewModelOf(::HabitCalendarVM)
    viewModelOf(::HabitDayVM)
    viewModelOf(::StatsVM)
    viewModelOf(::SettingsVM)

}
