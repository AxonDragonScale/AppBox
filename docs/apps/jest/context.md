---
title: "Jest — jokes app"
status: current
summary: "Jokes app on Compose Multiplatform (Android, JVM, iOS) with Room caching, a Ktor client, and an Android-only Glance widget. The reference KMP layout."
read_when: "Changing Jest, or copying its KMP structure and expect/actual patterns into another app."
---

> Context: [Main](../../context.md) > Jest

# Jest — Jokes App

## Overview

Jest is a joke/humor app within AppBox that fetches and displays jokes from an external API. It features a daily joke system, favorites management, history tracking, a settings screen, and a home screen widget.

- **Package**: `com.axondragonscale.jest`
- **Module path**: `:apps:jest:jestComposeApp`
- **Android shell**: `:apps:jest:jestAndroid` (application module; holds `applicationId` and the manifest)
- **Module directory**: `apps/jest/jestComposeApp/`
- **Platforms**: Android, Desktop (JVM), iOS
- **Key Tech**: Ktor (API calls), Room (caching), Koin (DI), Compose Multiplatform, Glance (Android widget)

## Architecture

- **UI Framework**: Compose Multiplatform with Material3
- **Navigation**: `NavHost` with manual `when` block — `JestApp` hosts a `NavHost`; `MainScreen` is the adaptive dashboard destination rendering content via `when (activeDestination)` (no HorizontalPager)
- **Dependency Injection**: Koin — each platform entry point starts Koin with `NetworkModule + JestModule` (jest is the only app that needs HTTP)
- **State Management**: ViewModel + StateFlow (`collectAsStateWithLifecycle`)
- **Persistence**: Room (KMP) + DataStore Preferences (via commons `createPreferencesDataStore`)
- **Networking**: Ktor client with kotlinx.serialization
- **Widget**: Jetpack Glance (Android-only, untouched by KMP migration)

## KMP Source Set Layout

```
jestComposeApp/src/
├── commonMain/          # All shared logic and UI
├── androidMain/         # JestActivity, JestApplication, widget, platform actuals
├── jvmMain/             # Desktop entry point + platform actuals
└── iosMain/             # iOS entry point + platform actuals
```

All business logic, UI, and DI live in `commonMain`. Platform source sets contain only entry points and `actual` implementations.

## Package Structure

### `commonMain`

```
com.axondragonscale.jest/
├── ShareUtils.kt                # expect fun shareText(text: String)
├── database/
│   ├── JestDatabase.kt          # @Database class + expect fun createJestDatabase()
│   ├── JestDbClient.kt          # Database operations wrapper
│   ├── converter/               # Room type converters
│   ├── dao/JokeDao.kt           # Data Access Object (includes getJokeCount, getFavoriteCount)
│   ├── entity/JokeEntity.kt     # Room entity
│   └── mapper/JokeMapper.kt     # Entity ↔ model mappers
├── di/
│   └── JestModule.kt            # Koin module (fully common — no platform types)
├── model/
│   ├── BlacklistFlag.kt         # Content filter flags enum
│   ├── Category.kt              # Joke categories enum
│   ├── Flags.kt                 # Joke content flags
│   ├── Joke.kt                  # IJoke sealed interface: OnePartJoke, TwoPartJoke
│   ├── JokeType.kt              # Single / TwoPart enum
│   ├── Language.kt              # Supported languages enum
│   └── serialization/           # Custom kotlinx serializer for IJoke
├── network/
│   ├── JokeApiClient.kt         # Ktor client for JokeAPI v2
│   ├── mapper/                  # API response → entity mappers
│   └── response/                # API response data classes
├── repository/
│   ├── JokeRepository.kt        # Core joke data operations (+ getJokeCount, getFavoriteCount)
│   └── PrefsRepository.kt       # User preferences via DataStore
└── ui/
    ├── JestApp.kt               # Root NavHost composable
    ├── MainScreen.kt            # Adaptive shell: Sidebar (tablet/desktop) or BottomBar (mobile)
    ├── common/
    │   ├── HeroJokeCard.kt      # Full-size joke card with shimmer loading state (Home screen)
    │   └── LibraryJokeCard.kt   # Compact card with category badge + timestamp (Library screen)
    ├── component/               # Reusable UI: FilterChip, StatCard, MultiSelector, Shimmer, etc.
    ├── devtools/JestDevTools.kt # Jest-specific DevTools screen (uses commons DevToolsScreen)
    ├── filter/                  # Filter system: PrefsVM + FilterBottomSheet + FilterPopover + FilterHost
    ├── home/                    # Home screen: HomeVM + HomeUiState + HomeUiEvent + Home composable
    ├── library/                 # Library screen: LibraryVM + LibraryUiState + LibraryUiEvent + Library composable
    ├── navigation/              # Destination sealed class, Route, BottomBar, Sidebar
    ├── settings/                # Settings screen: SettingsVM + SettingsUiState + SettingsComponents
    ├── theme/                   # Color.kt, Type.kt, Theme.kt (JestDarkColorScheme + JestLightColorScheme)
    └── util/                    # WindowSize enum + rememberWindowSize() (adaptive library)
```

### `commonMain/composeResources`

```
composeResources/
└── drawable/
    └── ic_github.xml            # GitHub icon (CMP resource, used in Settings)
```

### `androidMain`

```
com.axondragonscale.jest/
├── JestApplication.kt           # Application: AppInfo.init + Koin start
├── JestActivity.kt              # Activity: dynamic colors, system bars, JestTheme { JestApp() }
├── ShareUtils.android.kt        # actual shareText: Intent.ACTION_SEND chooser
├── database/
│   └── JestDatabase.android.kt  # actual createJestDatabase: Room.databaseBuilder + Context
└── widget/                      # Jetpack Glance widget (Android-only, unchanged)
    ├── JestAppWidget.kt
    ├── JestWidget.kt
    ├── JestWidgetActions.kt
    └── JestWidgetReceiver.kt
```

### `jvmMain`

```
com.axondragonscale.jest/
├── main.kt                      # AppInfo.init + startKoin + application { JestTheme { JestApp() } }
├── ShareUtils.jvm.kt            # actual shareText: clipboard copy via Toolkit
└── database/
    └── JestDatabase.jvm.kt      # actual createJestDatabase: Room.databaseBuilder + BundledSQLiteDriver
```

### `iosMain`

```
com.axondragonscale.jest/
├── MainViewController.kt        # ComposeUIViewController { JestTheme { JestApp() } } + initKoin()
├── ShareUtils.ios.kt            # actual shareText: UIActivityViewController
└── database/
    └── JestDatabase.ios.kt      # actual createJestDatabase: Room.databaseBuilder + NSHomeDirectory
```

## Key Components

### Navigation

`JestApp` hosts a `NavHost` with two destinations:
- `Route.Dashboard` → `MainScreen` (adaptive shell with 3 destinations)
- `Route.DevTools` → `JestDevTools`

`MainScreen` renders content via `when (activeDestination)` — no HorizontalPager. Navigation chrome adapts to window size:
- **Compact** (mobile): floating `BottomBar` over content
- **Medium** (tablet): fixed 240dp `Sidebar` on the left
- **Expanded** (desktop): fixed 280dp `Sidebar` on the left

Window size is determined by `rememberWindowSize()` from `ui/util/WindowSizeUtils.kt`, which uses `currentWindowAdaptiveInfo()` from `org.jetbrains.compose.material3.adaptive:adaptive:1.3.0-alpha02`.

### Destinations

| Destination | Screen | Notes |
|-------------|--------|-------|
| `Destination.Home` | `Home` | Hero joke card, stats, filter chips |
| `Destination.Library` | `Library` | All jokes / Favorites toggle, LazyColumn (mobile) or LazyVerticalGrid (tablet+) |
| `Destination.Settings` | `Settings` | Single-column (mobile), 2-column (tablet), 3-column (desktop) |

### Theme

`JestTheme` in `commonMain` accepts `darkTheme: Boolean` and an optional `colorScheme: ColorScheme?` override:
- **Android**: `JestActivity` resolves dynamic Material You colors (API 31+) and passes them as the override. Falls back to `JestDarkColorScheme` / `JestLightColorScheme` for system bar tinting when dynamic color is unavailable.
- **Desktop / iOS**: call `JestTheme` without an override; static dark/light scheme is used.

`JestDarkColorScheme` and `JestLightColorScheme` are `internal val` in `Theme.kt` (indigo-primary, amber-secondary, custom surface tokens).

`isSystemInDarkTheme()` is the default for `darkTheme` on all platforms.

### Filter System

The filter UI adapts based on `WindowSize`:
- **Compact**: `FilterBottomSheet` — `ModalBottomSheet` sliding up from the bottom
- **Medium / Expanded**: `FilterPopover` — floating `Popup` card (320dp wide)

`FilterHost` is the single call-site API — callers pass `filterType`, `windowSize`, and `onDismiss`; it selects the right surface automatically.

`FilterType` is a sealed interface with two objects:
- `FilterType.Category` — renders the Category sheet; accent color is indigo (primary)
- `FilterType.Flags` — renders the Exclude Flags sheet; accent color is amber (secondary)

Each sheet renders a vertical list of **checkbox rows** (not pill chips): 52dp tall, 10dp corner radius, 16dp horizontal padding. Selected rows use `DarkSurface` background; the right-side checkbox is a 22×22dp rounded square — filled with the accent color and showing a check icon when selected, or dark with a `#3A3A40` border when unselected.

The header row shows the filter title (Fraunces `titleLarge`) on the left and a "Clear" text link (accent-colored) on the right. A muted subtitle sits beneath. The "Apply Filters" CTA button at the bottom is full-width, 52dp tall, 14dp corner radius, filled with the accent color.

`PrefsVM` / `PrefsUiState` / `PrefsUiEvent` live in `ui/filter/` (moved from old `ui/tune/`).
`PrefsUiEvent` includes `CategoryCleared` and `FlagsCleared` to reset selections from the "Clear" button.

### Library Screen

Replaces the old separate `Favorites` and `History` tabs. `LibraryVM` exposes a single flow toggled by `LibraryFilter` enum (`All`, `Favorites`). Layout uses `LazyColumn` on Compact and `LazyVerticalGrid(columns = 2)` on Medium/Expanded.

### Stats (Home Screen)

`HomeUiState` carries `jokeCount: Int` and `favoriteCount: Int` sourced from `JokeDao.getJokeCount()` and `JokeDao.getFavoriteCount()` (both `Flow<Int>`). Day streak shows `—` placeholder (deferred).

### Window Size

`WindowSize` is a project-local `enum class { Compact, Medium, Expanded }` in `ui/util/WindowSize.kt`.

`rememberWindowSize()` in `ui/util/WindowSizeUtils.kt` calls `currentWindowAdaptiveInfo()` and checks breakpoints via `WindowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)` / `WIDTH_DP_EXPANDED_LOWER_BOUND`. This avoids the broken `androidx.compose.material3.windowsizeclass.WindowWidthSizeClass` companion object that was unresolvable in `commonMain`.

### `expect`/`actual` Summary

| Contract | `commonMain` | `androidMain` | `jvmMain` | `iosMain` |
|---|---|---|---|---|
| Share text | `expect fun shareText(text: String)` | Intent chooser | Clipboard copy | UIActivityViewController |
| Room DB creation | `expect fun createJestDatabase(): JestDatabase` | `Room.databaseBuilder(context)` | `Room.databaseBuilder(path)` + BundledSQLiteDriver | `Room.databaseBuilder(NSHomeDirectory)` |

Platform actuals for `AppInfo`, `DataStore`, and `DevToolsBuiltInContent` live in `:core:common`
(`AppInfo`), `:core:datastore` (DataStore) and `:core:devtools` (DevTools built-ins).

### Joke Model

Sealed interface `IJoke` with two implementations:
- `OnePartJoke` — single text joke (`joke` field)
- `TwoPartJoke` — setup and delivery (`setup`, `delivery` fields)

Both carry: `id`, `lang`, `category`, `type`, `flags`, `safe`, `favorite`, `timestamp`.

`getShareableText()` (internal) produces a formatted share string consumed by `shareText()` in `JokeCard`.

### JokeRepository

| Method | Description |
|--------|-------------|
| `getCurrentJoke()` | Returns cached joke or fetches new one if older than 1 day |
| `getNewJoke()` | Always fetches from API using current user preferences |
| `getAllJokes()` | Flow of all jokes (Library — All filter) |
| `getFavoriteJokes()` | Flow of favorited jokes (Library — Favorites filter) |
| `getJokeCount()` | Flow of total joke count (Home stats) |
| `getFavoriteCount()` | Flow of favorite joke count (Home stats) |
| `addToFavorites(id)` | Marks joke as favorite in DB |
| `removeFromFavorites(id)` | Unmarks favorite in DB |

`isOld()` on a `Joke` uses `kotlinx.datetime Clock.System.now()` (not `System.currentTimeMillis()`).

### JokeApiClient

Ktor client for JokeAPI v2:
- Base URL: `https://v2.jokeapi.dev`
- Endpoint: `/joke/{category}?amount=1&lang=en&blacklistFlags=...&type=...`
- Response deserialized via a custom `JokeSerializer` (handles `single` / `twopart` polymorphism)

### PrefsRepository

DataStore-backed preferences (via commons `createPreferencesDataStore("jest_prefs")`):

| Preference | Type | Description |
|------------|------|-------------|
| `jokeCategories` | `List<Category>` | Selected joke categories |
| `jokeTypes` | `List<JokeType>` | Selected joke types (single/twopart) |
| `blacklistFlags` | `List<BlacklistFlag>` | Content filters |
| `uiMode` | `UIMode` | Light / Dark / System |
| `dynamicTheme` | `Boolean` | Material You dynamic colors (Android) |

### JestModule (Koin)

Fully in `commonMain`. Wires:
- `createJestDatabase()` → `JestDatabase` singleton
- `JestDatabase.getJokeDao()` → `JokeDao` singleton
- `JestDbClient`, `JokeApiClient`, `PrefsRepository`, `JokeRepository` singletons
- `HomeVM`, `LibraryVM`, `PrefsVM`, `SettingsVM` viewModels (`FavoritesVM` and `HistoryVM` removed)

### JestDevTools

Uses commons `DevToolsScreen` with Jest-specific additions:
- App Info section displaying `AppInfo.versionName`, `AppInfo.versionCode`, `AppInfo.packageName`
- Accessible via Settings screen → "Open Dev Tools"

### Widget (Android-only)

Jetpack Glance home screen widget. Entirely in `androidMain`, unchanged by the KMP migration.
- `JestWidgetReceiver` — broadcast receiver, declared in `AndroidManifest.xml`
- `JestAppWidget` — Glance `GlanceAppWidget`
- Config: `res/xml/jest_widget_provider.xml`

## Database Schema

### JokeEntity

| Column | Type | Notes |
|--------|------|-------|
| `id` | `Int` | Primary key (from API) |
| `joke` | `String?` | Single-part joke text |
| `setup` | `String?` | Two-part joke setup |
| `delivery` | `String?` | Two-part joke delivery |
| `lang` | `String` | Language code (via `LanguageConverter`) |
| `category` | `String` | Joke category (via `CategoryConverter`) |
| `type` | `String` | `single` or `twopart` (via `JokeTypeConverter`) |
| `flags` | embedded | `Flags` object columns |
| `safe` | `Boolean` | Safe-for-work flag |
| `favorite` | `Boolean` | User favorite status |
| `timestamp` | `Long` | Epoch milliseconds when fetched |

Database version: `1`. Schema exported to `jestComposeApp/schemas/`.

## Data Flow

1. User opens Jest → `HomeVM` calls `JokeRepository.getCurrentJoke()`
2. Repository checks Room DB for a non-stale joke (fetched < 1 day ago)
3. If stale or absent, `JokeApiClient` fetches from JokeAPI using `PrefsRepository` settings
4. API response mapped to `JokeEntity`, upserted into Room
5. Entity mapped to `IJoke`, emitted to `HomeUiState`
6. `JokeCard` displays the joke; share button calls `shareText(joke.getShareableText())`
7. Favorite toggle → `JokeRepository.addToFavorites` / `removeFromFavorites`
8. Widget refresh → fetches new joke independently via its own repository call

## Build Configuration (`build.gradle.kts`)

**Targets**: `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`

The module applies `alias(libs.plugins.kmp.app.convention)`, which supplies the KMP/Compose plugins and target configuration. Dependencies are declared in this build file.

**`commonMain` dependencies** (highlights):
- `:core:common`, `:core:ui`, `:core:devtools`, `:core:datastore`, `:core:room`, `:core:network`
- Room runtime + SQLite bundled
- Ktor core + content negotiation + kotlinx.json serialization
- Compose Multiplatform (runtime, foundation, material3, ui, resources, tooling-preview)
- `org.jetbrains.compose.material3.adaptive:adaptive:1.3.0-alpha02` — `currentWindowAdaptiveInfo()` for window size
- `androidx.lifecycle` viewmodel-compose + runtime-compose
- Navigation Compose (multiplatform)
- Koin BOM + `koin-core` + `koin-compose` + `koin-compose-viewmodel`
- `kotlinx-coroutines-core`, `kotlinx-serialization-json`, `kotlinx-datetime`

**`androidMain`** adds: splash screen, Glance, `koin-android` bundle, OkHttp Ktor engine, Chucker, `kotlinx-coroutines-android`

**`jvmMain`** adds: `compose.desktop.currentOs`, CIO Ktor engine, `kotlinx-coroutines-swing`

**`iosMain`** adds: Darwin Ktor engine

**KSP** targets: `kspAndroid`, `kspIosSimulatorArm64`, `kspIosArm64`, `kspJvm` — all use `androidx.room.compiler`

## Code Conventions

- ViewModels: `HomeVM`, `LibraryVM`, `PrefsVM`, `SettingsVM`
- `koinViewModel()` from `org.koin.compose.viewmodel` (KMP-compatible, not `koin.androidx.compose`)
- `@Preview` from `androidx.compose.ui.tooling.preview` (NOT `org.jetbrains.compose.ui.tooling.preview` — the CMP version is not usable in commonMain for these files)
- Single `@Preview` per composable — no `uiMode` dark-mode variants (Android-only API)
- `expect`/`actual` file naming: `MyFile.kt` (common), `MyFile.android.kt`, `MyFile.jvm.kt`, `MyFile.ios.kt`
- All screen composables accept `windowSize: WindowSize` (not `WindowWidthSizeClass`) for adaptive layout

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Core Modules](../../core/context.md) — Shared utilities, AppInfo, DataStore factory, DevTools, networking, DI
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
