> Context: [Main](../../context.md) > Limitless

# Limitless — Habit Tracking App

## Overview

Limitless is a fully-featured habit tracking app built as a **Kotlin Multiplatform** module targeting Android, Desktop (JVM), and iOS. Users can create habits, record completions, configure goals and reminders, and explore rich statistics.

**Package**: `com.axondragonscale.limitless`  
**Module path**: `:apps:limitless:limitlessComposeApp`  
**Android shell**: `:apps:limitless:limitlessAndroid` (application module; holds `applicationId` and the manifest)  
**Targets**: `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`

## Architecture

- **UI**: Compose Multiplatform + Material 3 + custom Kodchasan font family
- **Navigation**: Navigation Compose with type-safe `@Serializable` routes
- **DI**: Koin — `initKoin()` in `commonMain`, platform config passed per-platform
- **Database**: Room KMP with `@ConstructedBy` constructor pattern; `createDatabase<LimitlessDatabase>()` from commons
- **Preferences**: DataStore Preferences via `createPreferencesDataStore()` from commons (no `Context`)
- **Reminders**: `expect`/`actual` `ReminderScheduler` — Android uses `AlarmManager`; Desktop/iOS are no-op stubs
- **Theme**: MaterialKolor (`rememberDynamicColorScheme`) with optional Material You override on Android 12+
- **Dev Tools**: `DevToolsScreen` from `:core:commons` with app-specific tools and `SampleDataSeeder`

## Source Set Layout

```
limitlessComposeApp/src/
├── commonMain/                          # 114 files — all business logic, UI, DB, DI
│   ├── composeResources/
│   │   ├── drawable/                    # ic_github.xml, limitless_icon_foreground.xml
│   │   └── font/                        # 12 Kodchasan .ttf variants
│   └── kotlin/com/axondragonscale/limitless/
│       ├── Link.kt                      # GitHub / source URLs
│       ├── core/
│       │   └── ReminderScheduler.kt     # expect class
│       ├── database/
│       │   ├── LimitlessDatabase.kt     # @ConstructedBy(LimitlessDatabaseConstructor)
│       │   ├── converter/               # GoalType, HabitColor, HabitView, ReminderType
│       │   ├── dao/                     # Habit, Record, HabitDayCount, HabitHourCount, Reminder, Goal
│       │   ├── entity/                  # Habit, Record, Reminder, Goal
│       │   └── view/                    # HabitDayCountView, HabitHourCountView
│       ├── di/
│       │   ├── KoinInit.kt              # initKoin(platformModule, config)
│       │   └── LimitlessModule.kt       # Common Koin bindings + all ViewModels
│       ├── model/                       # BitWeek, GoalStatus, GoalType, Habit, HabitColor,
│       │   │                            # HabitHeatMapData, HabitMaxCount, HabitView,
│       │   │                            # ReminderType, UIAlign
│       ├── repository/
│       │   ├── AppPrefsRepository.kt    # DataStore (no Context)
│       │   └── LimitlessRepository.kt
│       └── ui/
│           ├── LimitlessApp.kt          # Root NavHost
│           ├── Route.kt
│           ├── common/                  # AppBar, HabitCard, HabitMonthView, HabitWeekView,
│           │                            # HeatMapGrid, PickerChip, RecordHabitSheet,
│           │                            # RecordItem, SummaryStatCard
│           ├── day/                     # HabitDay screen + VM
│           ├── devtools/                # LimitlessDevTools + SampleDataSeeder
│           ├── editor/                  # Editor screen + VM, GoalsSection, RemindersSection,
│           │                            # NotificationPermission.kt (expect)
│           ├── habit/                   # Habit screen + VM, HabitTab
│           │   ├── calendar/            # HabitCalendar screen + VM
│           │   ├── history/             # History screen + VM
│           │   └── stats/               # HabitStats screen + VM + model/ + widget/
│           ├── home/                    # Home screen + VM
│           ├── model/                   # MonthCount, MonthlyTrendData, WeeklyPatternData
│           ├── settings/                # Settings screen + VM
│           ├── stats/                   # Stats screen + VM + model/ + widget/
│           └── theme/                   # Color.kt, HabitTheme.kt, Theme.kt, Type.kt
│
├── androidMain/                         # 9 files — Android-only
│   └── kotlin/com/axondragonscale/limitless/
│       ├── LimitlessActivity.kt         # Splash screen, edge-to-edge, Material You colors
│       ├── LimitlessApplication.kt      # AppInfo.init + initKoin { androidContext + androidLogger }
│       ├── core/
│       │   ├── BootReceiver.kt
│       │   ├── ReminderNotificationManager.kt
│       │   ├── ReminderReceiver.kt
│       │   └── ReminderScheduler.android.kt  # actual — full AlarmManager implementation
│       ├── di/
│       │   └── LimitlessAndroidModule.kt     # ReminderScheduler + ReminderNotificationManager
│       └── ui/
│           ├── editor/
│           │   └── NotificationPermission.android.kt  # actual for rememberNotificationPermissionState / rememberExactAlarmState
│           └── theme/
│               └── SystemBars.kt             # SystemBarsEffect(darkTheme) — status/nav bar tint
│
├── jvmMain/                             # 4 files
│   └── kotlin/com/axondragonscale/limitless/
│       ├── main.kt                      # Desktop entry — AppInfo.init + initKoin(LimitlessJvmModule)
│       ├── core/
│       │   └── ReminderScheduler.jvm.kt # actual — no-op
│       ├── di/
│       │   └── LimitlessJvmModule.kt    # single { ReminderScheduler() }
│       └── ui/editor/
│           └── NotificationPermission.jvm.kt  # actual — no-op, returns true
│
└── iosMain/                             # 4 files
    └── kotlin/com/axondragonscale/limitless/
        ├── MainViewController.kt        # iOS entry — initKoin(LimitlessIosModule) + LimitlessTheme
        ├── core/
        │   └── ReminderScheduler.ios.kt # actual — no-op
        ├── di/
        │   └── LimitlessIosModule.kt    # single { ReminderScheduler() }
        └── ui/editor/
            └── NotificationPermission.ios.kt  # actual — no-op, returns true
```

## Navigation Routes

```kotlin
object Route {
    @Serializable data object Home
    @Serializable data object Settings
    @Serializable data object Stats
    @Serializable data class Habit(val habitId: Long)
    @Serializable data class Editor(val habitId: Long? = null)   // null = create, non-null = edit
    @Serializable data class HabitDay(val habitId: Long, val date: String)
    @Serializable data object DevTools
}
```

Transitions use slide-up enter / fade-out exit / fade-in pop-enter / slide-down pop-exit.

## Koin DI

### `initKoin` (commonMain)

```kotlin
fun initKoin(platformModule: Module? = null, config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(buildList {
            add(CommonsModule)
            add(LimitlessModule)
            if (platformModule != null) add(platformModule)
        })
    }
}
```

### `LimitlessModule` (commonMain)

```kotlin
val LimitlessModule = module {
    single { createDatabase<LimitlessDatabase>(LimitlessDatabase.DB_NAME) }
    single { get<LimitlessDatabase>().habitDao() }
    single { get<LimitlessDatabase>().recordDao() }
    single { get<LimitlessDatabase>().habitDayCountDao() }
    single { get<LimitlessDatabase>().habitHourCountDao() }
    single { get<LimitlessDatabase>().reminderDao() }
    single { get<LimitlessDatabase>().goalDao() }
    singleOf(::AppPrefsRepository)
    singleOf(::LimitlessRepository)
    singleOf(::SampleDataSeeder)
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
```

### `LimitlessAndroidModule` (androidMain)

```kotlin
val LimitlessAndroidModule = module {
    singleOf(::ReminderScheduler)           // actual — Context + LimitlessRepository
    singleOf(::ReminderNotificationManager)
}
```

Platform modules (`LimitlessJvmModule`, `LimitlessIosModule`) bind the no-op `ReminderScheduler`.

## Database Schema

### Entities

#### HabitEntity (`habit`)

| Column | Type | Notes |
|--------|------|-------|
| id | Long | PK, auto-generated |
| name | String | Habit name |
| description | String | Habit description |
| color | HabitColor | Enum (stored as name string) |
| view | HabitView | Enum — HEAT_MAP (default), MONTH, WEEK |

#### RecordEntity (`record`)

| Column | Type | Notes |
|--------|------|-------|
| id | Long | PK, auto-generated |
| habitId | Long | FK → habit.id (CASCADE) |
| timestamp | Long | Epoch millis — `Clock.System.now().toEpochMilliseconds()` |
| message | String | Optional note |

Indices: `habitId`, `timestamp`, composite `(habitId, timestamp)`.

#### ReminderEntity (`reminder`)

| Column | Type | Notes |
|--------|------|-------|
| id | Long | PK, auto-generated |
| habitId | Long | FK → habit.id (CASCADE) |
| type | ReminderType | ONE_TIME, DAILY, WEEKLY, MONTHLY |
| time | LocalTime | Stored via `LocalTimeConverter` |
| date | LocalDate? | ONE_TIME only |
| daysOfWeek | BitWeek? | WEEKLY — bitmask of ISO days (bit 0 = Mon) |
| dayOfMonth | Int? | MONTHLY — 1..31, clamped to month length |

#### GoalEntity (`goal`)

| Column | Type | Notes |
|--------|------|-------|
| id | Long | PK, auto-generated |
| habitId | Long | FK → habit.id (CASCADE) |
| type | GoalType | DAILY, WEEKLY, MONTHLY |
| target | Int | Times-per-period goal |

Unique index on `(habitId, type)` — max one goal per type per habit.

### Database Views

#### `HabitDayCountView`

Pre-aggregates record counts per habit per calendar date. Used by heat maps and streak calculations.

```sql
SELECT habitId,
       DATE(timestamp / 1000, 'unixepoch', 'localtime') as date,
       COUNT(*) as count
FROM record
GROUP BY habitId, date
```

#### `HabitHourCountView`

Pre-aggregates record counts per habit per hour of day. Used by the Time of Day histogram in habit stats.

```sql
SELECT habitId,
       CAST(STRFTIME('%H', timestamp / 1000, 'unixepoch', 'localtime') AS INTEGER) AS hour,
       COUNT(*) AS count
FROM record
GROUP BY habitId, hour
```

DB version: **3**. Migration strategy: destructive (via commons `createDatabase` helper).

## Domain Models

| Model | Description |
|-------|-------------|
| `HabitColor` | 18-color enum with ARGB values; use `HabitColor.color` extension for Compose `Color` |
| `HabitView` | Display mode: HEAT_MAP, MONTH, WEEK |
| `GoalType` | Period for a goal: DAILY, WEEKLY, MONTHLY |
| `GoalStatus` | Pairs `GoalEntity` with computed `current`, `isAchieved`, `progress` |
| `ReminderType` | ONE_TIME, DAILY, WEEKLY, MONTHLY |
| `BitWeek` | `@JvmInline value class` wrapping an `Int` bitmask for day-of-week selection |
| `UIAlign` | App bar position: Top / Bottom |
| `HabitHeatMapData` | Habit + day-count map + maxCount for heat map rendering |
| `HabitMaxCount` | Habit + its maximum single-day record count |

## Preferences (DataStore)

Stored in `app_prefs_store` via commons `createPreferencesDataStore`:

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `uiMode` | String (enum name) | `UIMode.DEFAULT` | Light / Dark / System |
| `dynamicTheme` | Boolean | `false` | Material You dynamic colors (Android 12+ only) |
| `uiAlign` | String (enum name) | `UIAlign.Bottom` | App bar position |
| `highlightCurrentDay` | Boolean | `true` | Highlight today's cell on heat map / month views |

## Reminders System

`ReminderScheduler` is an `expect`/`actual` class:

- **Android actual** (`ReminderScheduler.android.kt`): full `AlarmManager.setExactAndAllowWhileIdle` implementation with `computeNextTriggerMillis` logic for all four reminder types.
- **JVM / iOS actuals**: no-op stubs.
- `BootReceiver` reschedules all alarms after device reboot.
- `ReminderReceiver` fires the notification via `ReminderNotificationManager`.
- `RemindersSection` composable uses `expect fun rememberNotificationPermissionState()` and `expect fun rememberExactAlarmState()` to keep permission UI in `commonMain`.

## Theme System

### `LimitlessTheme` (commonMain)

```kotlin
@Composable
fun LimitlessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme? = null,   // Android passes Material You colors; others use MaterialKolor
    content: @Composable () -> Unit,
)
```

Falls back to `rememberDynamicColorScheme(Color.Black, darkTheme, PaletteStyle.Monochrome)` when no override is provided.

### `HabitTheme` (commonMain)

Wraps habit-specific screens (`Habit`, `Editor`, `HabitDay`) with a full M3 scheme seeded from the habit's `HabitColor`. Derives dark/light from the parent scheme's surface luminance.

### Font

`kodchasanTypography()` — `@Composable` function that loads all 12 Kodchasan variants via `Res.font.*` (CMP resources). Applied globally in `LimitlessTheme`.

### Android system bars

`SystemBarsEffect(darkTheme)` in `androidMain/ui/theme/SystemBars.kt` applies `isAppearanceLightStatusBars` / `isAppearanceLightNavigationBars` and disables contrast enforcement on Android R+. Called from `LimitlessActivity` before `LimitlessTheme`.

## Key Component Notes

### `LimitlessActivity`

1. Installs splash screen, holds it while reading initial prefs via `runBlocking`.
2. Resolves `dynamicColorScheme` from `dynamicDarkColorScheme`/`dynamicLightColorScheme` on API 31+.
3. Calls `SystemBarsEffect`, then `LimitlessTheme(colorScheme = dynamicColorScheme)`.

### `AppBar`

Configurable top/bottom bar driven by `UIAlign`. Accepts `leadingItems` / `trailingItems` as `List<AppBarItem>`. Inserts `HorizontalDivider` on the correct edge automatically.

### `EditorVM`

- Temporary IDs for unsaved reminders/goals use plain `var` counters starting at -1, decrementing on each add (not `AtomicLong` — VM event handling is sequential).
- On save: diffs current vs original reminder IDs, cancels deleted ones via `ReminderScheduler.cancel`, saves new/updated ones, then schedules them via `ReminderScheduler.schedule`.
- Uses `Channel<EditorUiEffect>` for one-shot navigation effects.

### `HabitStatsVM`

Computes streak data from `HabitDayCountView` using a sorted date list: walks backwards from today for current streak, scans forward for best streak.

### `SampleDataSeeder`

Creates 7 sample habits with varied colors, views, goals, and historical records spanning several months. Accessed via `LimitlessDevTools`.

## UI Pattern Conventions

- **Screen composables**: outer function wires VM + navigation, calls inner stateless function that is previewable
- **VM naming**: `HomeVM`, `EditorVM`, `HabitVM`, `HistoryVM`, `HabitCalendarVM`, `HabitDayVM`, `HabitStatsVM`, `StatsVM`, `SettingsVM`
- **Events**: `vm.onEvent(event)` for all state mutations; navigation events handled at composable layer
- **State collection**: `collectAsStateWithLifecycle()` from `androidx.lifecycle.runtime.compose` (KMP-compatible)
- **ViewModel injection**: `koinViewModel()` from `org.koin.compose.viewmodel` (KMP-compatible)
- **Previews**: `@Preview(showBackground = true)` + `@Preview(showBackground = true, uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)` for light/dark pairs
- **Data processing**: done in repository layer (combine flows, transform views), not in ViewModels

## Build Configuration

- **Namespace**: `com.axondragonscale.limitless`
- **Min SDK**: 31 (Android 12)
- **JVM target**: 21
- **Convention plugin**: `kmp-app-convention` (Compose module) / `android-app-convention` (Android shell)
- **KSP**: declared in the Compose module for `kspAndroid`, `kspIosSimulatorArm64`, `kspIosArm64`, `kspJvm` (all configured for Room)
- **Compose resources**: `packageOfResClass = "com.axondragonscale.limitless.resources"`
- **Room schema dir**: `$projectDir/schemas`
- **Desktop main class**: `com.axondragonscale.limitless.MainKt`
- **`buildConfig`** is generated in the `limitlessAndroid` shell (the Android-KMP library plugin has no BuildConfig); runtime app info uses `AppInfo` from commons

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, module index
- [Commons Module](../../core/commons/context.md) — `createDatabase`, `createPreferencesDataStore`, `AppInfo`, `DevToolsScreen`
- [Jest Context](../jest/context.md) — Reference KMP implementation (same patterns)
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog