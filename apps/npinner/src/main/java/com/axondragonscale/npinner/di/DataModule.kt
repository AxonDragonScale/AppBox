package com.axondragonscale.npinner.di

import com.axondragonscale.npinner.core.NPinnerNotificationManager
import com.axondragonscale.npinner.core.NPinnerNotificationMonitor
import com.axondragonscale.npinner.core.NPinnerNotificationScheduler
import com.axondragonscale.npinner.data.NPinnerDatabase
import com.axondragonscale.npinner.repository.AppPrefsRepository
import com.axondragonscale.npinner.repository.NotificationRepository
import com.axondragonscale.npinner.ui.notificationEditor.NotificationEditorViewModel
import com.axondragonscale.npinner.ui.notifications.NotificationsViewModel
import com.axondragonscale.npinner.ui.notifications.themePicker.ThemePickerViewModel
import com.axondragonscale.npinner.work.monitor.MonitorWorker
import com.axondragonscale.npinner.work.schedule.ScheduleWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val NPinnerModule = module {
    // Database
    single { NPinnerDatabase.getInstance(androidContext()) }
    single { get<NPinnerDatabase>().notificationDao() }

    // Repositories
    single { NotificationRepository(get()) }
    single { AppPrefsRepository(androidContext()) }

    // Core services
    single { NPinnerNotificationManager(androidContext()) }
    single { NPinnerNotificationScheduler(androidContext()) }
    single { NPinnerNotificationMonitor(androidContext(), get(), get()) }

    // ViewModels
    viewModel { NotificationsViewModel(get(), get(), get()) }
    viewModel { ThemePickerViewModel(get()) }
    viewModel { params -> NotificationEditorViewModel(params.get(), get(), get(), get()) }

    // Workers
    worker { MonitorWorker(androidContext(), get(), get()) }
    worker { ScheduleWorker(androidContext(), get(), get(), get()) }
}
