> Context: [Main](../../context.md) > Limitless

# Limitless - Habit Tracking App

## Overview

Limitless is a habit tracking app within AppBox that allows users to create, manage, and track daily habits. It features a habit editor, statistics view, and customizable UI with theme and layout preferences.

**Package**: `com.axondragonscale.limitless`
**Module path**: `:apps:limitless`

## Architecture

- **UI**: Jetpack Compose + Material 3 (custom Kodchasan font family)
- **Navigation**: Navigation Compose with type-safe `@Serializable` routes
- **DI**: Koin (`LimitlessApplication` initializes `CommonsModule` + `LimitlessModule`)
- **Database**: Room (habits + records tables, with `HabitDayCountView` database view)
- **Preferences**: DataStore Preferences (theme, layout)
- **Dev Tools**: Common `DevToolsScreen` from `:core:commons` with app-specific info

## Package Structure

```
com.axondragonscale.limitless/
├── LimitlessActivity.kt           # Entry point with splash screen + theme
├── LimitlessApplication.kt        # Koin DI initialization
├── LimitlessConstants.kt          # GitHub links
├── database/
│   ├── LimitlessDatabase.kt       # Room database (habits + records)
│   ├── dao/
│   │   ├── HabitDao.kt            # CRUD for habits
│   │   └── RecordDao.kt           # CRUD for records
│   └── entity/
│       ├── HabitEntity.kt         # Habit table (id, name, description)
│       └── RecordEntity.kt        # Record table (id, habitId, timestamp, message)
├── di/
│   └── LimitlessModule.kt         # Koin module
├── model/
│   ├── Habit.kt                   # Domain model
│   ├── UIAlign.kt                 # App bar position (Top/Bottom)
│   └── UIMode.kt                  # Theme (Light/Dark/Auto)
├── repository/
│   ├── AppPrefsRepository.kt      # DataStore preferences
│   └── LimitlessRepository.kt     # Habit data operations
├── ui/
│   ├── LimitlessApp.kt            # Root NavHost composable
│   ├── Route.kt                   # Type-safe routes
│   ├── common/
│   │   ├── AppBar.kt              # Custom app bar (configurable position)
│   │   └── Dropdown.kt            # Generic dropdown component
│   ├── devtools/
│   │   └── LimitlessDevTools.kt   # Wraps DevToolsScreen with app-specific tools
│   ├── editor/                    # Habit create/edit screen
│   ├── home/                      # Main habit list screen
│   ├── settings/                  # Settings screen
│   ├── stats/                     # Statistics screen (stub)
│   ├── theme/                     # Kodchasan font, Purple/Pink colors
│   └── util/                      # Debug border, flow extensions
└── util/
    ├── FlowExt.kt                 # Flow.collect() no-op extension
    └── Log.kt                     # Logging utilities
```

## Navigation Routes

```kotlin
object Route {
    @Serializable data object Home          // Main habit list
    @Serializable data object Settings      // App preferences
    @Serializable data class Editor(val habitId: Long? = null)  // Create or edit habit
    @Serializable data object Stats         // Statistics (stub)
    @Serializable data object DevTools      // Developer tools
}
```

Transitions use slide-up enter / fade-out exit animations.

## Koin Module

```kotlin
val LimitlessModule = module {
    singleOf(LimitlessDatabase::create)
    singleOf(LimitlessDatabase::habitDao)
    singleOf(LimitlessDatabase::recordDao)
    singleOf(::AppPrefsRepository)
    singleOf(::LimitlessRepository)
    viewModelOf(::HomeVM)
    viewModelOf(::EditorVM)
    viewModelOf(::StatsVM)
    viewModelOf(::SettingsVM)
}
```

## Database Schema

### HabitEntity

| Column | Type | Notes |
|--------|------|-------|
| id | Long | Primary key, auto-generated |
| name | String | Habit name |
| description | String | Habit description |

### RecordEntity

| Column | Type | Notes |
|--------|------|-------|
| id | Long | Primary key, auto-generated |
| habitId | Long | Foreign key → HabitEntity.id (CASCADE delete) |
| timestamp | Long | Epoch millis when record was created |
| message | String | Record message/note |

**Indices**: `habitId`, `timestamp`, composite `(habitId, timestamp)`

### HabitDayCountView (Database View)

Pre-aggregated view for heat map calculations (10-20x faster than in-memory processing):

```sql
SELECT
    habitId,
    DATE(timestamp / 1000, 'unixepoch', 'localtime') as date,
    COUNT(*) as count,
    MAX(COUNT(*)) OVER (PARTITION BY habitId) as maxCount
FROM record
GROUP BY habitId, date
```

Automatically updates when records are inserted/deleted.

## Key Components

### LimitlessActivity

Reads initial theme preferences (UI mode + dynamic theme) synchronously during splash screen via `runBlocking`, then observes them reactively for runtime changes.

### AppBar Component

Custom composable replacing standard TopAppBar/BottomAppBar:
- Configurable position via `UIAlign` (Top or Bottom)
- Accepts `leadingItems` and `trailingItems` as lists of `AppBarItem` (icon + onClick)
- Auto-places a `HorizontalDivider` on the appropriate edge based on alignment

### Theme System

- Light / Dark / Auto (system) modes via `UIMode` enum
- Dynamic color (Material You) toggle, available on Android 12+
- Falls back to Purple/Pink static color scheme when dynamic color is off
- Custom **Kodchasan** font family (12 variants) applied globally via Material 3 Typography

### EditorVM — One-shot Effects Pattern

Uses `Channel<EditorUiEffect>` for navigation side effects:
```kotlin
private val uiEffectChannel = Channel<EditorUiEffect>()
val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)
```

## Preferences (DataStore)

Stored in `app_prefs_store`:

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `uiMode` | Int (ordinal) | `UIMode.AUTO` | Light / Auto / Dark |
| `dynamicTheme` | Boolean | `false` | Material You dynamic colors |
| `uiAlign` | Int (ordinal) | `UIAlign.Bottom` | App bar position |

## Data Flow

### Habit Management
1. User opens Limitless → `HomeVM` exposes `HomeUiState`
2. Adding a habit → navigates to `Route.Editor()` (no habitId = create mode)
3. Editing a habit → navigates to `Route.Editor(habitId = X)` (edit mode)
4. On save → `LimitlessRepository.saveHabit()` upserts to Room → `EditorUiEffect.NavigateBack` emitted

### Heat Map Data Flow (Optimized)
1. `HomeVM` calls `repository.getHabitHeatMapData()`
2. Repository combines `habits` Flow + `dayCounts` Flow from `HabitDayCountView`
3. Database view provides pre-aggregated data (grouping/counting done in SQLite)
4. Flow automatically updates when records change → UI reactively updates

### Settings
Settings changes flow through `AppPrefsRepository` → DataStore → observed reactively in Activity/VMs

## UI Pattern Conventions

- **ViewModel naming**: `HomeVM`, `EditorVM`, `StatsVM`, `SettingsVM`
- **Navigation events**: Handled at the composable layer, forwarded to `navController`
- **All other events**: Delegated to `vm.onEvent(event)`
- **Composable screens**: Two-layer pattern — outer wires VM + navigation, inner is stateless and previewable
- **Data processing**: Handled in Repository layer, not in ViewModels

## Dependencies

- `projects.core.commons` — Shared utilities, logger, network, DI module, DevToolsScreen
- AndroidX Compose BOM + Material 3
- AndroidX Navigation Compose
- Koin, Room, DataStore Preferences
- kotlinx.serialization, kotlinx.coroutines

## Build Info

- **Namespace**: `com.axondragonscale.limitless`
- **Min SDK**: 31 (Android 12)
- **JVM**: 21
- **Convention Plugin**: `app-convention`

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, module index
- [Commons Module](../../core/commons/context.md) — Shared utilities, DevToolsScreen, DI module
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog