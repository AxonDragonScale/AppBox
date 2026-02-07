# AppBox - Project Context

## Overview

AppBox is a monorepo containing multiple standalone Android applications that share common utilities and build configuration. Each app is independently installable and publishable while benefiting from shared code and consistent architecture.

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
| Build System | Gradle (Kotlin DSL) with Version Catalogs + Convention Plugins |

## Project Structure

```
AppBox/
├── apps/                   # Standalone Android applications
│   ├── jest/              # Jokes/humor app with widget support
│   ├── wallmatic/         # Wallpaper auto-changer app
│   └── topty/             # 2FA authenticator app
├── core/                   # Shared library modules
│   └── commons/           # Shared utilities, network, DI setup
├── build-logic/           # Convention plugins for build configuration
│   └── src/main/kotlin/
│       ├── app-convention.gradle.kts      # For app modules
│       └── library-convention.gradle.kts  # For library modules
├── gradle/                # Version catalog (libs.versions.toml)
└── keystore/              # Signing configuration
```

## App Modules

### `:apps:jest` (Jokes App)
- **Package**: `com.axondragonscale.jest`
- **Application Class**: `JestApplication`
- **Purpose**: Daily jokes/humor content with home screen widget
- **Key Tech**: Ktor (API calls), Room (caching), Glance (widget)

### `:apps:wallmatic` (Wallpaper Manager)
- **Package**: `com.axondragonscale.wallmatic`
- **Application Class**: `WallmaticApplication`
- **Purpose**: Auto-change wallpapers on schedule
- **Key Tech**: Room (persistence), Proto DataStore (settings), DocumentFile (SAF), Quick Settings Tiles

### `:apps:topty` (Authenticator)
- **Package**: `com.axondragonscale.topty`
- **Application Class**: `ToptyApplication`
- **Purpose**: TOTP-based 2FA code manager
- **Key Tech**: Room (storage), CameraX + ML Kit (QR scanning)

## Core Modules

### `:core:commons` (Shared Library)
- **Package**: `com.axondragonscale.commons`
- **Purpose**: Shared utilities across all apps
- **Contains**: 
  - Logger utilities
  - Result wrapper
  - Serialization helpers
  - Network setup (Ktor client)
  - Common Compose utilities
  - CommonsModule (Koin DI module)

## Build Configuration

### Convention Plugins

**`app-convention`** - Applied to all app modules:
- Android Application plugin
- Kotlin Android + Compose
- Signing configuration (shared keystore)
- Common SDK versions and compile options
- BuildConfig and Compose features enabled

**`library-convention`** - Applied to library modules:
- Android Library plugin
- Kotlin Android
- Common SDK versions and compile options

### Dependency Injection Setup

Each app has its own `Application` class that initializes Koin:

```kotlin
class JestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@JestApplication)
            modules(
                CommonsModule,  // From :core:commons
                JestModule,     // App-specific module
            )
        }
    }
}
```

## Architecture Patterns

- **UI Layer**: Jetpack Compose with Material 3
- **State Management**: ViewModel + StateFlow
- **Dependency Injection**: Koin (not Hilt/Dagger)
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Networking**: Ktor Client with Kotlinx Serialization JSON

## Coding Conventions

### Module Guidelines
- Each app is fully self-contained with its own Application class
- Shared code goes in `:core:commons`
- App modules depend on `:core:commons`, not on each other
- Use typesafe project accessors (`projects.core.commons`)

## Building

```bash
# Build all apps
./gradlew assembleDebug

# Build specific app
./gradlew :apps:jest:assembleDebug
./gradlew :apps:wallmatic:assembleDebug
./gradlew :apps:topty:assembleDebug
```

## Key Dependencies Reference

```kotlin
// Convention plugin (for apps)
plugins {
    id("app-convention")
}

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

// Core module
implementation(projects.core.commons)
```

## Splash Screen Configuration

Each app has its own splash screen theme defined in `res/values/themes.xml`:

```xml
<style name="Theme.AppName" parent="android:Theme.Material.Light.NoActionBar">
    <item name="android:windowSplashScreenBackground">@color/splash_background</item>
    <item name="android:windowSplashScreenAnimatedIcon">@drawable/app_icon_foreground</item>
</style>
```

Activities call `installSplashScreen()` before `super.onCreate()`.

## Notes for AI Assistance

1. **Always use Koin** for dependency injection, not Hilt or Dagger
2. **Prefer Ktor** for networking, not Retrofit
3. **Use kotlinx.serialization** for JSON, not Gson or Moshi
4. **Target JVM 21** - modern Kotlin features are available
5. **Room with KSP** - not KAPT
6. **Proto DataStore** is used in wallmatic for typed settings
7. When creating new features, check if similar patterns exist in other apps
8. The project uses **Compose BOM** - don't specify individual Compose versions
9. Each app is independent - no shared Application class
10. Use convention plugins (`app-convention`, `library-convention`) for new modules
11. Apps are in `apps/` directory, libraries are in `core/` directory
