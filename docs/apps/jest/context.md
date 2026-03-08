> Context: [Main](../../context.md) > Jest

# Jest — Jokes App

## Overview

Jest is a joke/humor app within AppBox that fetches and displays jokes from an external API. It features a daily joke system, favorites management, history tracking, a settings screen, and a home screen widget.

- **Package**: `com.axondragonscale.jest`
- **Module path**: `:apps:jest`
- **Module directory**: `apps/jest/jestComposeApp/`
- **Platforms**: Android, Desktop (JVM), iOS
- **Key Tech**: Ktor (API calls), Room (caching), Koin (DI), Compose Multiplatform, Glance (Android widget)

## Architecture

- **UI Framework**: Compose Multiplatform with Material3
- **Navigation**: `NavHost` with `HorizontalPager` — `JestApp` hosts a `NavHost`; `MainScreen` is the dashboard destination containing a `HorizontalPager` with a custom bottom tab bar
- **Dependency Injection**: Koin — each platform entry point starts Koin with `CommonsModule + JestModule`
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
│   ├── dao/JokeDao.kt           # Data Access Object
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
│   ├── JokeRepository.kt        # Core joke data operations
│   └── PrefsRepository.kt       # User preferences via DataStore
└── ui/
    ├── JestApp.kt               # Root NavHost composable
    ├── MainScreen.kt            # HorizontalPager dashboard
    ├── bottombar/               # Tab definitions + BottomTabBar composable
    ├── common/JokeCard.kt       # Shared joke card component
    ├── component/               # Reusable UI components (buttons, tags, selectors, shimmer)
    ├── devtools/JestDevTools.kt # Jest-specific DevTools screen (uses commons DevToolsScreen)
    ├── home/                    # Home screen: VM + UiState + UiEvent + composable
    ├── list/                    # Favorites & History screens: shared JokeList composable + VMs
    ├── navigation/Route.kt      # Typed navigation routes
    ├── settings/                # Settings screen: VM + UiState + UiEvent + composable
    ├── theme/                   # Color.kt, Type.kt, Theme.kt (common JestTheme)
    └── tune/                    # Prefs bottom sheet: VM + UiState + UiEvent + composable
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
- `Route.Dashboard` → `MainScreen` (HorizontalPager with four tabs)
- `Route.DevTools` → `JestDevTools`

`MainScreen` uses `HorizontalPager` (not Navigation Compose) to switch between tabs. The `BottomTabBar` floats over the pager content.

### Tabs

| Position | Tab | Screen |
|----------|-----|--------|
| 0 | Home | `Home` |
| 1 | Favorites | `Favorites` (via `JokeList`) |
| 2 | History | `History` (via `JokeList`) |
| 3 | Settings | `Settings` |

### Theme

`JestTheme` in `commonMain` accepts `darkTheme: Boolean` and an optional `colorScheme: ColorScheme?` override:
- **Android**: `JestActivity` resolves dynamic Material You colors (API 31+) and passes them as the override
- **Desktop / iOS**: call `JestTheme` without an override; static purple-based scheme is used

`isSystemInDarkTheme()` is the default for `darkTheme` on all platforms.

### `expect`/`actual` Summary

| Contract | `commonMain` | `androidMain` | `jvmMain` | `iosMain` |
|---|---|---|---|---|
| Share text | `expect fun shareText(text: String)` | Intent chooser | Clipboard copy | UIActivityViewController |
| Room DB creation | `expect fun createJestDatabase(): JestDatabase` | `Room.databaseBuilder(context)` | `Room.databaseBuilder(path)` + BundledSQLiteDriver | `Room.databaseBuilder(NSHomeDirectory)` |

Platform actuals for `AppInfo`, `DataStore`, and `DevToolsBuiltInContent` live in `:core:commons`.

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
| `getAllJokes()` | Flow of all jokes (History tab) |
| `getFavoriteJokes()` | Flow of favorited jokes (Favorites tab) |
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
- `HomeVM`, `FavoritesVM`, `HistoryVM`, `PrefsVM`, `SettingsVM` viewModels

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

**Targets**: `androidTarget`, `jvm`, `iosArm64`, `iosSimulatorArm64`

**`commonMain` dependencies** (highlights):
- `projects.core.commons`
- Room runtime + SQLite bundled
- Ktor core + content negotiation + kotlinx.json serialization
- Compose Multiplatform (runtime, foundation, material3, ui, resources, tooling-preview)
- `androidx.lifecycle` viewmodel-compose + runtime-compose
- Navigation Compose (multiplatform)
- Koin BOM + `koin-core` + `koin-compose` + `koin-compose-viewmodel`
- `kotlinx-coroutines-core`, `kotlinx-serialization-json`

**`androidMain`** adds: splash screen, Glance, `koin-android` bundle, OkHttp Ktor engine, Chucker, `kotlinx-coroutines-android`

**`jvmMain`** adds: `compose.desktop.currentOs`, CIO Ktor engine, `kotlinx-coroutines-swing`

**`iosMain`** adds: Darwin Ktor engine

**KSP** targets: `kspAndroid`, `kspIosSimulatorArm64`, `kspIosArm64`, `kspJvm` — all use `androidx.room.compiler`

## Code Conventions

- ViewModels: `HomeVM`, `FavoritesVM`, `HistoryVM`, `PrefsVM`, `SettingsVM`
- `koinViewModel()` from `org.koin.compose.viewmodel` (KMP-compatible, not `koin.androidx.compose`)
- `@Preview` from `org.jetbrains.compose.ui.tooling.preview` (CMP, not `androidx.compose.ui.tooling.preview`)
- Single `@Preview` per composable — no `uiMode` dark-mode variants (Android-only API)
- `expect`/`actual` file naming: `MyFile.kt` (common), `MyFile.android.kt`, `MyFile.jvm.kt`, `MyFile.ios.kt`

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Commons Module](../../core/commons/context.md) — Shared utilities, AppInfo, DataStore factory, DevTools, networking, DI
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
- [KMP Migration Plan](./kmp-migration-plan.md) — Completed migration reference