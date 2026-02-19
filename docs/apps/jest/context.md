> Context: [Main](../../context.md) > Jest

# Jest - Jokes App

## Overview

Jest is a joke/humor app within AppBox that fetches and displays jokes from an external API. It features a daily joke system, favorites management, history tracking, and a home screen widget.

- **Package**: `com.axondragonscale.jest`
- **Module path**: `:apps:jest`
- **Key Tech**: Ktor (API calls), Room (caching), Glance (widget)

## Architecture

- **UI Framework**: Jetpack Compose with Material3
- **Navigation**: HorizontalPager with custom bottom tab bar (not Navigation Compose)
- **Dependency Injection**: Koin (`JestApplication` initializes `CommonsModule` + `JestModule`)
- **State Management**: ViewModel + StateFlow
- **Persistence**: Room database + DataStore Preferences
- **Networking**: Ktor client with kotlinx.serialization
- **Widget**: Jetpack Glance

## Package Structure

```
com.axondragonscale.jest/
├── JestActivity.kt              # Entry point activity
├── database/
│   ├── JestDatabase.kt          # Room database definition
│   ├── JestDbClient.kt          # Database operations wrapper
│   ├── converter/               # Room type converters
│   ├── dao/                     # Data Access Objects
│   ├── entity/                  # Database entities
│   └── mapper/                  # Entity to model mappers
├── di/
│   └── JestModule.kt            # Koin dependency injection module
├── model/
│   ├── BlacklistFlag.kt         # Content filter flags
│   ├── Category.kt              # Joke categories enum
│   ├── Flags.kt                 # Joke content flags
│   ├── Joke.kt                  # Joke model (IJoke, OnePartJoke, TwoPartJoke)
│   ├── JokeType.kt              # Single/TwoPart joke types
│   ├── Language.kt              # Supported languages
│   └── serialization/           # Custom serializers
├── network/
│   ├── JokeApiClient.kt         # Ktor HTTP client for JokeAPI
│   ├── mapper/                  # API response to entity mappers
│   └── response/                # API response models
├── repository/
│   ├── JokeRepository.kt        # Joke data operations
│   └── PrefsRepository.kt       # User preferences (DataStore)
├── ui/
│   ├── JestApp.kt               # Root composable with HorizontalPager
│   ├── bottombar/               # Bottom navigation tab bar
│   ├── common/                  # Shared UI components
│   ├── component/               # Reusable components
│   ├── home/                    # Home screen (current joke display)
│   ├── list/                    # Favorites & History screens
│   ├── theme/                   # Jest-specific theming
│   └── tune/                    # Preferences/settings screen
└── widget/
    ├── JestAppWidget.kt         # Widget UI definition
    ├── JestWidget.kt            # Widget composable
    ├── JestWidgetActions.kt     # Widget interaction handlers
    └── JestWidgetReceiver.kt    # Widget broadcast receiver
```

## Key Components

### JestActivity

Entry point activity with edge-to-edge display. Requests notification permission for Chucker (network inspection in debug builds).

```kotlin
companion object {
    fun start(context: Context) {
        val intent = Intent(context, JestActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
```

### Navigation (Tabs)

Three main screens accessible via swipeable HorizontalPager:
- **Home**: Displays current joke with refresh capability
- **Favorites**: List of saved favorite jokes
- **History**: All previously fetched jokes

### Joke Model

Sealed interface `IJoke` with two implementations:
- `OnePartJoke`: Single text joke (`joke` field)
- `TwoPartJoke`: Setup and delivery format (`setup`, `delivery` fields)

Both include metadata: `id`, `lang`, `category`, `type`, `flags`, `safe`, `favorite`, `timestamp`

### Joke Categories

```kotlin
enum class Category {
    Programming, Miscellaneous, Dark, Pun, Spooky, Christmas
}
```

### Content Filters (BlacklistFlags)

Users can filter out jokes containing: nsfw, religious, political, racist, sexist, explicit

### JokeRepository

Core data operations:
- `getCurrentJoke()`: Returns cached joke or fetches new if old (>1 day)
- `getNewJoke()`: Fetches from API with user preferences
- `getAllJokes()`: Flow of all jokes (history)
- `getFavoriteJokes()`: Flow of favorites
- `addToFavorites()` / `removeFromFavorites()`

### JokeApiClient

Ktor client connecting to JokeAPI v2:
- Base URL: `https://v2.jokeapi.dev`
- Endpoints: `/joke/{category}` with query parameters
- Parameters: `amount`, `lang`, `blacklistFlags`, `type`

### Widget (Glance)

Home screen widget displaying jokes:
- Registered via `JestWidgetReceiver`
- Configuration in `res/xml/jest_widget_provider.xml`
- Supports refresh action

## Database Schema

### JokeEntity

Room entity storing jokes:

| Column | Type | Notes |
|--------|------|-------|
| id | Int | Primary key (from API) |
| joke | String? | Single-part joke text |
| setup | String? | Two-part joke setup |
| delivery | String? | Two-part joke delivery |
| lang | String | Language code |
| category | String | Joke category |
| type | String | `single` or `twopart` |
| flags | Flags | Content flags (embedded) |
| safe | Boolean | Safe-for-work flag |
| favorite | Boolean | User favorite status |
| timestamp | Long | Epoch millis when fetched |

## Preferences (DataStore)

Stored in `app_prefs` DataStore:

| Key | Type | Description |
|-----|------|-------------|
| `jokeCategories` | Set<Int> | Selected category ordinals |
| `jokeTypes` | Set<Int> | Selected joke type ordinals |
| `blacklistFlags` | Set<Int> | Content filter ordinals |

## Data Flow

1. User opens Jest → `HomeVM` calls `JokeRepository.getCurrentJoke()`
2. Repository checks DB for recent joke (< 1 day old)
3. If no valid joke, fetches from API via `JokeApiClient`
4. Response mapped to `JokeEntity`, inserted into Room
5. Entity mapped to `IJoke` model, displayed in UI
6. User can favorite → updates `favorite` flag in DB
7. Widget can trigger refresh → fetches new joke

## Dependencies

- **Local Modules**: `projects.core.commons`
- **AndroidX**: Compose BOM + Material3, Jetpack Glance
- **Koin**: DI
- **Room**: Database
- **Ktor**: Networking
- **Chucker**: Network debugging (debug builds)
- **kotlinx.serialization**: JSON parsing

## Build Info

- **Namespace**: `com.axondragonscale.jest`
- **Min SDK**: 31 (Android 12)

## Code Conventions

- ViewModels: `HomeVM`, `FavoritesVM`, `HistoryVM`, `PrefsVM`
- Custom serializers for sealed class joke types
- Extension functions for joke sharing (`getShareIntent()`)

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Commons Module](../../core/commons/context.md) — Shared utilities, networking, DI module
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog