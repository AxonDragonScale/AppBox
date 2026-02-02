# AppBox - Project Context

## Overview

AppBox is a multi-module Android "super-app" that consolidates multiple small applications and utilities into a single codebase. The project follows a modular architecture where each sub-app is an independent Android library module.

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin (JVM 21) |
| Min SDK | 31 (Android 12) |
| Target SDK | 36 |
| UI Framework | Jetpack Compose |
| DI Framework | Koin |
| Networking | Ktor + Kotlinx Serialization |
| Database | Room |
| DataStore | Preferences + Proto DataStore |
| Image Loading | Coil 3 |
| Widgets | Jetpack Glance |
| Build System | Gradle (Kotlin DSL) with Version Catalogs |

## Project Structure

```
AppBox/
├── app/                    # Main application module (shell app)
├── commons/                # Shared utilities, network, DI setup
├── wallmatic/              # Wallpaper auto-changer sub-app
├── jest/                   # Jokes/humor sub-app with widget support
├── topty/                  # 2FA authenticator sub-app
├── dev-butler/             # Developer utilities (internal)
├── build-logic/            # Convention plugins for build configuration
├── gradle/                 # Version catalog (libs.versions.toml)
└── keystore/               # Signing configuration
```

## Module Details

### `:app` (Main Shell)
- **Package**: `com.axondragonscale.appbox`
- **Purpose**: Entry point, navigation hub, integrates all sub-apps
- **Key Dependencies**: All feature modules, Navigation Compose, DataStore Prefs

### `:commons` (Shared Library)
- **Package**: `com.axondragonscale.commons`
- **Purpose**: Shared utilities across all modules
- **Contains**: Logger, Result wrapper, Serialization helpers, Network setup, DI modules

### `:wallmatic` (Wallpaper Manager)
- **Package**: `com.axondragonscale.wallmatic`
- **Purpose**: Auto-change wallpapers on schedule
- **Key Tech**: Room (persistence), Proto DataStore (settings), DocumentFile (SAF), Coil (images)

### `:jest` (Jokes App)
- **Package**: `com.axondragonscale.jest`
- **Purpose**: Daily jokes/humor content
- **Key Tech**: Ktor (API calls), Room (caching), Glance (home screen widget)

### `:topty` (Authenticator)
- **Package**: `com.axondragonscale.topty`
- **Purpose**: TOTP-based 2FA code manager
- **Status**: Minimal setup (early development)

### `:dev-butler` (Developer Tools)
- **Package**: `com.axondragonscale.dev.butler`
- **Purpose**: Internal developer utilities
- **Status**: Minimal setup

### `:build-logic` (Build Conventions)
- **Purpose**: Shared Gradle convention plugins
- **Contains**: Reusable build configuration
- **Status**: ⚠️ Currently NOT in use due to issues with accessing `libs.versions.toml` from convention plugins

## Architecture Patterns

- **UI Layer**: Jetpack Compose with Material 3
- **State Management**: Compose State + ViewModels
- **Dependency Injection**: Koin (not Hilt/Dagger)
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Networking**: Ktor Client with Kotlinx Serialization JSON

## Coding Conventions

### Kotlin Style
- Use Kotlin idioms (scope functions, extension functions, sealed classes)
- Prefer `Flow` for reactive streams
- Use `suspend` functions for one-shot async operations
- Leverage Kotlin serialization for JSON

### Compose Style
- Use Material 3 theming
- Prefer stateless composables with state hoisting
- Use `remember` and `derivedStateOf` appropriately
- Follow slot-based API patterns for reusable components

### Module Guidelines
- Each sub-app should be self-contained
- Shared code goes in `:commons`
- Feature modules depend on `:commons`, not on each other
- Use typesafe project accessors (`projects.commons` instead of `":commons"`)

## Build Configuration

- **Compile SDK**: 36 (release)
- **Java/Kotlin Target**: 21
- **Version Catalog**: `gradle/libs.versions.toml`
- **Feature Preview**: `TYPESAFE_PROJECT_ACCESSORS`

## Key Dependencies Reference

```kotlin
// DI
implementation(platform(libs.koin.bom))
implementation(libs.bundles.koin)

// Networking
implementation(libs.bundles.ktor)

// Database
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)

// Compose
implementation(platform(libs.androidx.compose.bom))
implementation(libs.bundles.compose)

// Images
implementation(libs.bundles.coil)

// Widgets
implementation(libs.bundles.glance)
```

## Notes for AI Assistance

1. **Always use Koin** for dependency injection, not Hilt or Dagger
2. **Prefer Ktor** for networking, not Retrofit
3. **Use kotlinx.serialization** for JSON, not Gson or Moshi
4. **Target JVM 21** - modern Kotlin features are available
5. **Room with KSP** - not KAPT
6. **Proto DataStore** is used in wallmatic for typed settings
7. When creating new features, check if similar patterns exist in other modules
8. The app uses **Compose BOM** - don't specify individual Compose versions
9. Debug builds have `.debug` application ID suffix