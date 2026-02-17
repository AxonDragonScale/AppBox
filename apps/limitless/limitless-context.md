# Limitless - Module Context

## Overview

Limitless is a habit tracking sub-app within AppBox that allows users to create, manage, and track daily habits. It features a habit editor, statistics view, and customizable UI with theme and layout preferences.

## Package Structure

```
com.axondragonscale.limitless/
├── LimitlessActivity.kt           # Entry point activity with splash screen + theme
├── LimitlessApplication.kt        # Application class with Koin DI initialization
├── LimitlessConstants.kt          # App-level constants (GitHub links)
├── database/
│   ├── LimitlessDatabase.kt       # Room database definition (habits + records)
│   ├── dao/
│   │   ├── HabitDao.kt            # CRUD operations for habits
│   │   └── RecordDao.kt           # CRUD operations for records
│   └── entity/
│       ├── HabitEntity.kt         # Habit table entity (id, name, description)
│       └── RecordEntity.kt        # Record table entity (id, habitId, timestamp, message)
├── di/
│   └── LimitlessModule.kt         # Koin dependency injection module
├── model/
│   ├── Habit.kt                   # Habit domain model
│   ├── UIAlign.kt                 # UI alignment preference (Top/Bottom)
│   └── UIMode.kt                  # Theme mode preference (Light/Dark/Auto)
├── repository/
│   ├── AppPrefsRepository.kt      # User preferences via DataStore (theme, layout)
│   └── LimitlessRepository.kt     # Habit data operations
├── ui/
│   ├── LimitlessApp.kt            # Root composable with NavHost
│   ├── Route.kt                   # Type-safe navigation routes
│   ├── common/
│   │   ├── AppBar.kt              # Custom app bar with leading/trailing icon items
│   │   └── Dropdown.kt            # Generic dropdown selector component
│   ├── devtools/
│   │   └── LimitlessDevTools.kt   # Wraps common DevToolsScreen with app-specific tools
│   ├── editor/
│   │   ├── Editor.kt              # Habit create/edit screen
│   │   ├── EditorUiEffect.kt      # One-shot effects (NavigateBack)
│   │   ├── EditorUiEvent.kt       # User actions (UpdateHabit, SaveHabit, NavigateBack)
│   │   ├── EditorUiState.kt       # Editor state (isLoading, habit)
│   │   └── EditorVM.kt            # Editor ViewModel with Channel-based effects
│   ├── home/
│   │   ├── Home.kt                # Home screen with habit list
│   │   ├── HomeUiEvent.kt         # User actions (NavigateToSettings, NavigateToStats, AddHabit)
│   │   ├── HomeUiState.kt         # Home state (isLoading)
│   │   └── HomeVM.kt              # Home ViewModel
│   ├── settings/
│   │   ├── Settings.kt            # Settings screen (appearance, behavior, about, dev tools)
│   │   ├── SettingsUiEvent.kt     # User actions (UIModeUpdated, DynamicThemeToggled, NavigateToDevTools)
│   │   ├── SettingsUiState.kt     # Settings state (uiMode, dynamicTheme)
│   │   └── SettingsVM.kt          # Settings ViewModel
│   ├── stats/
│   │   ├── Stats.kt               # Statistics screen (stub)
│   │   ├── StatsUiEvent.kt        # User actions (empty)
│   │   ├── StatsUiState.kt        # Stats state (isLoading)
│   │   └── StatsVM.kt             # Stats ViewModel (stub)
│   ├── theme/
│   │   ├── Color.kt               # Color palette (Purple/Pink scheme)
│   │   ├── Theme.kt               # LimitlessTheme with dynamic color support
│   │   └── Type.kt                # Kodchasan custom font family + typography
│   └── util/
│       ├── Debug.kt               # Debug border modifier
│       ├── FlowExt.kt             # collectWithLifecycle composable for one-shot effects
│       └── ModifierExt.kt         # Conditional modifier extension (thenIf)
└── util/
    ├── FlowExt.kt                 # Flow.collect() no-op extension
    └── Log.kt                     # Logging utilities (logD, logE, logI, etc.)
```

## Architecture

- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Navigation Compose with type-safe routes (kotlinx.serialization) 
- **Dev Tools**: Common `DevToolsScreen` from `:core:commons`
- **Dependency Injection**: Koin
- **State Management**: ViewModel + MutableStateFlow
- **One-shot Effects**: Channel + receiveAsFlow (EditorVM)
- **Persistence**: Room database + DataStore Preferences
- **Custom Typography**: Kodchasan font family

## Key Components

### LimitlessActivity
Entry point with edge-to-edge display. Reads initial theme preferences (UI mode + dynamic theme) synchronously during splash screen via `runBlocking`, then observes them reactively for runtime changes.

### Navigation (Routes)
Five screens navigated via `NavHost`:
- **Home** (`Route.Home`): Main screen displaying habits with bottom app bar
- **Settings** (`Route.Settings`): App preferences and about info
- **Editor** (`Route.Editor`): Create or edit a habit (accepts optional `habitId`)
- **Stats** (`Route.Stats`): Statistics view (currently a stub)
- **DevTools** (`Route.DevTools`): Common developer tools screen with app-specific info

Transitions use slide-up enter / fade-out exit animations with reverse pop transitions.

### Koin Module
```kotlin
val LimitlessModule = module {
    // Database
    singleOf(LimitlessDatabase::create)
    singleOf(LimitlessDatabase::habitDao)
    singleOf(LimitlessDatabase::recordDao)

    // Repository
    singleOf(::AppPrefsRepository)
    singleOf(::LimitlessRepository)

    // ViewModel
    viewModelOf(::HomeVM)
    viewModelOf(::EditorVM)
    viewModelOf(::StatsVM)
    viewModelOf(::SettingsVM)

}
```

`CommonsModule` from `:core:commons` is also loaded alongside `LimitlessModule` in the Application class.

### Theme System
- Supports Light / Dark / Auto (system) modes via `UIMode` enum
- Dynamic color (Material You) toggle, available on Android 12+
- Falls back to a Purple/Pink static color scheme when dynamic color is off
- Custom `Kodchasan` font family applied globally via Material 3 Typography
- Status bar and navigation bar appearance adapts to current theme

### AppBar Component
A custom composable replacing standard TopAppBar/BottomAppBar:
- Configurable position via `UIAlign` (Top or Bottom)
- Accepts `leadingItems` and `trailingItems` as lists of `AppBarItem` (icon + onClick)
- Auto-places a `HorizontalDivider` on the appropriate edge based on alignment

### EditorVM — One-shot Effects Pattern
Uses a `Channel<EditorUiEffect>` for navigation side effects:
```kotlin
private val uiEffectChannel = Channel<EditorUiEffect>()
val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)
```
Consumed in Compose via a custom `Flow.collectWithLifecycle` extension.

## Dependencies

### Local Modules
- `projects.core.commons` — Shared utilities, logger, network, DI module, DevToolsScreen

### Key Libraries
- AndroidX Compose BOM + Material 3
- AndroidX Navigation Compose
- Koin (DI)
- Room (database)
- DataStore Preferences
- kotlinx.serialization (type-safe navigation routes)
- kotlinx.coroutines

## Build Configuration

- **Namespace**: `com.axondragonscale.limitless`
- **Application ID**: `com.axondragonscale.limitless`
- **Min SDK**: 31 (Android 12)
- **Target SDK**: 36
- **Java/Kotlin**: JVM 21
- **Convention Plugin**: `app-convention`

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
A pre-aggregated view that computes daily record counts per habit:
| Column | Type | Notes |
|--------|------|-------|
| habitId | Long | Habit ID |
| date | LocalDate | Date in YYYY-MM-DD format (converted via TypeConverter) |
| count | Int | Number of records for this habit on this date |
| maxCount | Int | Maximum count for this habit (computed via SQL window function) |

**SQL Definition**:
```sql
SELECT
    habitId,
    DATE(timestamp / 1000, 'unixepoch', 'localtime') as date,
    COUNT(*) as count,
    MAX(COUNT(*)) OVER (PARTITION BY habitId) as maxCount
FROM record
GROUP BY habitId, date
```

This view automatically updates when records are inserted/deleted and provides significant performance improvements (10-20x faster) for heat map calculations.

## Preferences (DataStore)

Stored in `app_prefs_store`:
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `uiMode` | Int (ordinal) | `UIMode.AUTO` | Light / Auto / Dark |
| `dynamicTheme` | Boolean | `false` | Material You dynamic colors |
| `uiAlign` | Int (ordinal) | `UIAlign.Bottom` | App bar position |

## Data Flow

1. User opens Limitless → `LimitlessActivity` reads theme prefs during splash screen
2. `LimitlessApp` renders `NavHost` starting at `Route.Home`
3. `HomeVM` exposes `HomeUiState` — navigation events handled in composable layer
4. Adding a habit → navigates to `Route.Editor()` (no habitId = create mode)
5. Editing a habit → navigates to `Route.Editor(habitId = X)` (edit mode)
6. `EditorVM` loads existing habit from `LimitlessRepository` or creates a blank `HabitEntity`
7. On save → `LimitlessRepository.saveHabit()` upserts to Room → `EditorUiEffect.NavigateBack` emitted
8. Settings changes flow through `AppPrefsRepository` → DataStore → observed reactively in Activity/VMs
9. Settings → "Open Dev Tools" navigates to `Route.DevTools` → renders common `DevToolsScreen` with built-in utilities (Kill App, Clear Data, Permissions) + Limitless app info

### Heat Map Data Flow (Optimized)
1. `HomeVM` calls `repository.getHabitHeatMapData()`
2. Repository combines `habits` Flow + `dayCounts` Flow from `HabitDayCountView`
3. Database view provides pre-aggregated data (grouping/counting done in SQLite)
4. Repository transforms view data into `HabitHeatMapData` (minimal processing)
5. Flow automatically updates when records change → UI reactively updates
6. All heavy computation happens in SQL, not in memory

## Resources

### Custom Font
- **Kodchasan** — Full family (12 variants: Regular, Bold, Medium, SemiBold, Light, ExtraLight + Italic for each)
- Located in `res/font/kodchasan_*.ttf`

### Launcher Icon
- Foreground: `res/drawable/limitless_icon_foreground.xml`
- Background: `#FFFFFF`
- Adaptive icon defined in `res/mipmap-anydpi/limitless_icon.xml`

### Other Drawables
- `ic_github.xml` — GitHub icon used in Settings about section

### Constants
- `Link.LIMITLESS` = `https://github.com/AxonDragonScale/Limitless`
- `Link.AXON_DRAGON_SCALE` = `https://github.com/AxonDragonScale`

## UI Pattern Conventions

- **ViewModel naming**: `HomeVM`, `EditorVM`, `StatsVM`, `SettingsVM`
- **UiState/UiEvent/UiEffect**: Each screen has its own sealed interface for events, data class for state, and optionally a sealed interface for one-shot effects
- **Navigation events**: Handled at the composable layer (not inside VMs), forwarded to `navController`
- **All other events**: Delegated to `vm.onEvent(event)`
- **Composable screens**: Two-layer pattern — outer composable wires VM + navigation, inner composable is stateless and previewable
- **Data processing**: Handled in Repository layer, not in ViewModels — VMs focus on state management and business logic