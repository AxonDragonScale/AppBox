# NPinner - Notification Pinner Module

## Overview
NPinner is a notification pinning app within AppBox that lets users create persistent notifications pinned to the Android notification panel. Supports scheduling (one-time and recurring), dark mode theming, and survives device reboots/app updates.

## Architecture
- **UI**: Jetpack Compose + Material3 (Monospace typography throughout)
- **Navigation**: Navigation Compose with string routes and deep links (`npinner://` scheme)
- **DI**: Koin (`NPinnerApplication` initializes `CommonsModule` + `NPinnerModule`)
- **Database**: Room (single table, soft deletes via `deletedAt`)
- **Preferences**: DataStore Preferences (dark mode only)
- **Workers**: WorkManager with Koin worker factory
- **Permissions**: Accompanist Permissions (POST_NOTIFICATIONS)

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
│   ├── NPinnerDatabase.kt          # Room DB, version 1, singleton with synchronized init
│   ├── converter/                   # LocalDate, LocalTime, ScheduleType converters
│   ├── dao/
│   │   └── NotificationDao.kt      # Upsert, getAll (Flow), getPinned, getById, updatePinStatus
│   └── entity/
│       └── NotificationEntity.kt    # Schedule embedded via @Embedded
│   └── mapper/
│       └── NotificationEntityMapper.kt  # Extension fns: .toEntity(), .toModel() (+ List variants)
├── di/
│   └── DataModule.kt               # NPinnerModule val — all Koin registrations
├── model/
│   ├── DarkModeConfig.kt           # enum: ON, AUTO, OFF
│   ├── NPinnerNotification.kt      # Domain model with UUID id, soft-delete support
│   ├── Schedule.kt                 # date (LocalDate) + time (LocalTime) + type (ScheduleType?)
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
│   │   ├── Scheduler.kt            # Date/time pickers + repeat toggle (DAY/WEEK/MONTH)
│   │   └── nav/NotificationEditorNav.kt
│   ├── notifications/
│   │   ├── NotificationItem.kt     # List item with pin toggle + schedule badge + swipe-to-delete
│   │   ├── Notifications.kt        # Main list screen with swipe-to-dismiss
│   │   ├── NotificationsUiState.kt # Loading | Success(notifications)
│   │   ├── NotificationsViewModel.kt
│   │   ├── about/AboutBottomSheet.kt     # App info + GitHub links
│   │   ├── nav/NotificationsNav.kt
│   │   └── themePicker/
│   │       ├── ThemePickerBottomSheet.kt
│   │       ├── ThemePickerUiState.kt
│   │       └── ThemePickerViewModel.kt
│   ├── theme/                      # Dynamic color, Monospace font family, custom Dimen constants
│   └── util/Debug.kt               # debugBorder() modifier
├── util/
│   ├── ContextExt.kt               # systemNotificationManager, openNotificationSettings()
│   └── LocalDateTimeExt.kt         # Formatters: LocalDate.formatted, LocalTime.formatted, Long.relativeTimeSpan
└── work/
    ├── monitor/
    │   ├── MonitorRequest.kt        # Periodic 30-min work request
    │   └── MonitorWorker.kt         # Calls monitor.ensurePinnedNotificationVisibility()
    └── schedule/
        ├── ScheduleNotificationRequest.kt  # OneTimeWork with delay until scheduled time
        └── ScheduleWorker.kt        # Pins notification + reschedules if recurring
```

## Koin Module (`NPinnerModule`)
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

### Schedule (embedded in entity)
| Field | Type | Notes |
|-------|------|-------|
| date | LocalDate | Room converter: String ↔ LocalDate |
| time | LocalTime | Room converter: String ↔ LocalTime |
| type | ScheduleType? | null = one-time, non-null = recurring |

- `isFuture`: true if `type != null` (recurring always valid) or datetime is in the future
- Default: 10 minutes from now, repeat daily

### Database
- Single table `notifications`, version 1, `exportSchema = true`
- `Schedule` is `@Embedded` in `NotificationEntity`
- Queries filter `deletedAt IS NULL`, sort pinned first then by `updatedAt DESC`

## Key Behaviors

### Notification Lifecycle
1. **Create**: New UUID → editor → save → if no schedule, auto-pin + post notification
2. **Pin/Unpin**: Toggle updates DB + posts/dismisses system notification
3. **Schedule**: WorkManager one-time work with delay → pins + posts on trigger
4. **Recurring**: After trigger, ScheduleWorker adds 1 day/week/month and re-enqueues
5. **Delete**: Soft-delete (sets `deletedAt`) + dismiss notification + cancel scheduled work
6. **Swipe-to-dismiss**: In list, triggers full delete flow

### Notification Persistence
- **Boot**: `DeviceBootReceiver` → `NPinnerNotificationMonitor.ensurePinnedNotificationVisibility()`
- **App Update**: `AppUpdateReceiver` → same monitor call
- **Periodic**: `MonitorWorker` runs every 30 min to re-show any missing pinned notifications
- **Activity Resume**: `NPinnerActivity.onCreate` also triggers monitor + enqueues periodic work

### BroadcastReceivers
All receivers implement `KoinComponent` and use `by inject()` for dependencies.
- `UnpinNotificationReceiver`: action `npinner.unpin_notification`, dismisses + updates pin status
- `DeleteNotificationReceiver`: action `npinner.delete_notification`, dismisses + soft-deletes
- `AppUpdateReceiver`: action `MY_PACKAGE_REPLACED`
- `DeviceBootReceiver`: action `BOOT_COMPLETED`

### System Notifications
- Channel: `NPinner_Pinned_Notifications_Channel`, importance LOW, no badge/lights
- Ongoing (non-dismissable by user)
- Actions: "Unpin" and "Delete" via broadcast PendingIntents
- Content tap → deep link to editor

### Dark Mode
- Three modes: ON, AUTO (system), OFF
- Stored in DataStore as int ordinal
- `NPinnerActivity` collects flow and passes to theme
- Theme picker in bottom sheet from notifications screen

## AndroidManifest Notes
- `InitializationProvider` removed (`tools:node="remove"`) — WorkManager initialized by Koin's `workManagerFactory()`
- `windowSoftInputMode="adjustResize"` on activity for keyboard handling
- All receivers are not exported

## UI Patterns
- Fixed `TopBar` (56dp) and `BottomBar` (64dp) with content scrolling between them
- `BottomBar`: main action button (full-width) + left/right icon actions
- `SegmentedToggleButton`: used for ScheduleType and DarkModeConfig selection
- `NPinnerModalBottomSheet`: wraps Material3 BottomSheet for theme picker and about
- Dynamic color enabled by default (Android 12+)

## Dependencies
- AndroidX: Compose BOM, Navigation, Room (KSP), DataStore Preferences, Splash Screen, WorkManager
- Koin: BOM + full bundle (includes workmanager, compose, navigation)
- Accompanist: Permissions
- Coil: Image loading (bundled but minimally used)
- kotlinx: serialization, coroutines

## Build Info
- **Namespace**: `com.axondragonscale.npinner`
- **Application ID**: `com.axondragonscale.npinner`
- **Version**: 1.0.0 (versionCode 1)