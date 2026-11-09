---
title: "NPinner — notification pinner"
status: current
summary: "Pins persistent notifications to the panel with one-time and recurring schedules that survive reboots and app updates."
read_when: "Changing pinning, scheduling, or the notification UI."
---

> Context: [Main](../../context.md) > NPinner

# NPinner - Notification Pinner App

## Overview

NPinner is a notification pinning app within AppBox that lets users create persistent notifications pinned to the Android notification panel. Supports scheduling (one-time and recurring), dark mode theming, and survives device reboots/app updates.

- **Package**: `com.axondragonscale.npinner`
- **Module path**: `:apps:npinner`
- **Key Tech**: Room (persistence), WorkManager (scheduling), BroadcastReceivers (persistence across reboots)

## Architecture

- **UI**: Jetpack Compose + Material3 (Monospace typography throughout)
- **Navigation**: Navigation Compose with string routes and deep links (`npinner://` scheme)
- **DI**: Koin (`NPinnerApplication` initializes `NPinnerModule`)
- **Database**: Room (single table, soft deletes via `deletedAt`)
- **Date/Time**: `kotlin.time.Clock` + `kotlin.time.Instant` for clock/instant; `kotlinx.datetime` for `LocalDate`, `LocalTime`, `LocalDateTime`, `TimeZone`, `DateTimeUnit`
- **Preferences**: DataStore Preferences (dark mode only)
- **Workers**: WorkManager with Koin worker factory
- **Permissions**: Accompanist Permissions (POST_NOTIFICATIONS)
- **Notifications**: See [notification-system.md](./notification-system.md) for the full notification lifecycle, scheduling, persistence, and receiver details

## Package Structure

```
com.axondragonscale.npinner/
├── NPinnerActivity.kt              # Entry point, injects monitor + prefs via Koin
├── NPinnerApplication.kt           # Koin init with workManagerFactory()
├── NPinnerConstants.kt             # GitHub links
├── core/
│   ├── IntentProvider.kt           # PendingIntents for notification actions (object)
│   ├── NPinnerNotificationManager.kt  # Post/dismiss system notifications, channel setup
│   ├── NPinnerNotificationMonitor.kt  # Ensures pinned notifications stay visible
│   ├── NPinnerNotificationScheduler.kt # Enqueue/cancel WorkManager schedule requests
│   └── receiver/
│       ├── AppUpdateReceiver.kt        # MY_PACKAGE_REPLACED → re-show pinned
│       ├── DeleteNotificationReceiver.kt  # Notification action → soft-delete
│       ├── DeviceBootReceiver.kt       # BOOT_COMPLETED → re-show pinned
│       └── UnpinNotificationReceiver.kt   # Notification action → unpin
├── data/
│   ├── NPinnerDatabase.kt          # Room DB, version 1, synchronized singleton init
│   ├── converter/                   # LocalDate, LocalTime, ScheduleType converters
│   ├── dao/
│   │   └── NotificationDao.kt      # Upsert, getAll (Flow), getPinned, getById, updatePinStatus
│   ├── entity/
│   │   └── NotificationEntity.kt   # Schedule embedded via @Embedded
│   └── mapper/
│       └── NotificationEntityMapper.kt  # .toEntity(), .toModel() extensions
├── di/
│   └── DataModule.kt               # NPinnerModule val — all Koin registrations
├── model/
│   ├── DarkModeConfig.kt           # enum: ON, AUTO, OFF
│   ├── NPinnerNotification.kt      # Domain model with UUID id, soft-delete support
│   ├── Schedule.kt                 # date + time + type (nullable = one-time)
│   └── ScheduleType.kt             # enum: DAY, WEEK, MONTH
├── repository/
│   ├── AppPrefsRepository.kt       # DataStore: darkModeConfig read/write
│   └── NotificationRepository.kt   # CRUD over NotificationDao with timestamp management
├── ui/
│   ├── Destination.kt              # Route definitions with deeplink patterns
│   ├── NPinnerApp.kt               # NavHost with slide transitions
│   ├── common/                     # BottomBar, TopBar, Divider, TextField, Pickers, etc.
│   │   └── NotificationPermission.kt  # Runtime permission request + rationale dialog
│   ├── notificationEditor/
│   │   ├── Editor.kt               # Title (max 72 chars) + Description (max 400 chars)
│   │   ├── NotificationEditor.kt   # Full editor screen with save/delete actions
│   │   ├── NotificationEditorUiState.kt  # Loading | Success(createNew, notification)
│   │   ├── NotificationEditorViewModel.kt  # Uses SavedStateHandle for nav args
│   │   └── Scheduler.kt            # Date/time pickers + repeat toggle
│   ├── notifications/
│   │   ├── NotificationItem.kt     # List item with pin toggle + schedule badge + swipe-to-delete
│   │   ├── Notifications.kt        # Main list screen with swipe-to-dismiss
│   │   ├── NotificationsUiState.kt # Loading | Success(notifications)
│   │   ├── NotificationsViewModel.kt
│   │   ├── about/AboutBottomSheet.kt     # App info + GitHub links
│   │   └── themePicker/            # Dark mode selection bottom sheet
│   ├── theme/                      # Dynamic color, Monospace font, custom Dimen constants
│   └── util/Debug.kt               # debugBorder() modifier
├── util/
│   ├── ContextExt.kt               # systemNotificationManager, openNotificationSettings()
│   └── LocalDateTimeExt.kt         # Formatters: LocalDate.formatted, LocalTime.formatted
└── work/
    ├── monitor/
    │   ├── MonitorRequest.kt        # Periodic 30-min work request
    │   └── MonitorWorker.kt         # Re-shows missing pinned notifications
    └── schedule/
        ├── ScheduleNotificationRequest.kt  # OneTimeWork with delay
        └── ScheduleWorker.kt        # Pins notification + reschedules if recurring
```

## Koin Module

```kotlin
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
```

## Navigation & Deep Links

```
npinner://notifications          → Notifications list (start destination)
npinner://notificationEditor     → Create new notification
npinner://notificationEditor?id= → Edit existing notification (ACTION_EDIT deep link)
```

- `NotificationEditorViewModel` reads `id` from `SavedStateHandle`; null = create mode
- Notification tap opens editor via `IntentProvider.getNotificationEditorPendingIntent()`

## Data Model

### NPinnerNotification

| Field | Type | Notes |
|-------|------|-------|
| id | String | UUID, used as notification ID via `.hashCode()` |
| title | String | Required (save button disabled when blank) |
| description | String | Optional |
| isPinned | Boolean | Default true; controls system notification visibility |
| schedule | Schedule? | null = no schedule |
| createdAt | Long | Epoch millis, set on first save |
| updatedAt | Long | Epoch millis, updated on every upsert |
| deletedAt | Long? | Soft delete — non-null means deleted |

### Schedule (embedded in entity via `@Embedded`)

| Field | Type | Notes |
|-------|------|-------|
| date | `kotlinx.datetime.LocalDate` | Room converter: ISO String ↔ LocalDate |
| time | `kotlinx.datetime.LocalTime` | Room converter: ISO String ↔ LocalTime |
| type | ScheduleType? | null = one-time, non-null = recurring (DAY/WEEK/MONTH) |

Key properties:
- `asLocalDateTime`: combines date + time via `LocalDateTime(date, time)`
- `isFuture`: true if `type != null` (recurring always valid) or datetime is in the future
- **Default**: 10 minutes from now (`Clock.System.now().plus(10.minutes)`), repeat daily

### Database

- Single table `notifications`, version 1, `exportSchema = true`
- Queries filter `deletedAt IS NULL`, sort pinned first then by `updatedAt DESC`

## Dark Mode

- Three modes: `ON`, `AUTO` (system), `OFF` via `DarkModeConfig` enum
- Stored in DataStore as int ordinal
- `NPinnerActivity` collects flow and passes to theme
- Theme picker in bottom sheet from notifications screen
- Dynamic color enabled by default (Android 12+)

## UI Patterns

- Fixed `TopBar` (56dp) and `BottomBar` (64dp) with content scrolling between them
- `BottomBar`: main action button (full-width) + left/right icon actions
- `SegmentedToggleButton`: used for ScheduleType and DarkModeConfig selection
- `NPinnerModalBottomSheet`: wraps Material3 BottomSheet for theme picker and about
- Monospace font family throughout the app
- Swipe-to-dismiss on notification list items triggers full delete flow

## Date/Time Notes

- Uses **kotlinx-datetime** (0.7.1+), NOT java.time
- `Clock` and `Instant` are from `kotlin.time` (moved from kotlinx-datetime in 0.7.0)
- `LocalDate`, `LocalTime`, `LocalDateTime`, `TimeZone`, `DateTimeUnit` are from `kotlinx.datetime`
- Extension `Instant.toLocalDateTime(TimeZone)` imported from `kotlinx.datetime.toLocalDateTime`
- Extension `LocalDateTime.toInstant(TimeZone)` imported from `kotlinx.datetime.toInstant`
- Formatting uses `kotlinx.datetime.format` extension + `LocalDate.Format { }` / `LocalTime.Format { }` builders
- Room converters use ISO string format
- WorkManager delay: `(scheduledInstant - Clock.System.now()).inWholeMilliseconds`

## AndroidManifest Notes

- `InitializationProvider` removed (`tools:node="remove"`) — WorkManager initialized by Koin's `workManagerFactory()`
- `windowSoftInputMode="adjustResize"` on activity for keyboard handling
- All receivers are not exported

## Dependencies

- **Local Modules**: `:core:android`, `:core:common`, `:core:ui`, `:core:datastore`, `:core:room`
- **AndroidX**: Compose BOM, Navigation, Room (KSP), DataStore Preferences, Splash Screen, WorkManager
- **Koin**: BOM + full bundle (includes workmanager, compose, navigation)
- **Accompanist**: Permissions
- **Coil**: Image loading (bundled but minimally used)
- **kotlinx**: serialization, coroutines, datetime

## Build Info

- **Namespace**: `com.axondragonscale.npinner`
- **Application ID**: `com.axondragonscale.npinner`
- **Min SDK**: 31 (Android 12)
- **Version**: 1.0.0 (versionCode 1)

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Core Modules](../../core/context.md) — Shared utilities, networking, DI module
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
- **Sub-contexts:**
  - [Notification System](./notification-system.md) — Notification lifecycle, scheduling, persistence, receivers, workers