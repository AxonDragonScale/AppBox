# AppBox - App Module Context

## Overview

AppBox is an Android application that serves as a container/launcher for multiple sub-apps. It provides a unified entry point with a home screen displaying all available sub-apps and a settings screen for app-wide configuration.

## Package Structure

```
com.axondragonscale.appbox/
├── MainActivity.kt          # Entry point activity
├── MainApplication.kt       # Application class with Koin DI setup
├── data/
│   └── AppSettings.kt       # DataStore-based preferences
├── di/
│   └── AppModule.kt         # Koin dependency injection module
├── model/
│   └── AppThemeMode.kt      # Theme mode enum (SYSTEM, LIGHT, DARK)
└── ui/
    ├── App.kt               # Root composable with navigation
    ├── AppUiState.kt        # UI state data class
    ├── AppViewModel.kt      # Main ViewModel
    ├── home/
    │   └── HomeScreen.kt    # Home screen with sub-app grid
    ├── settings/
    │   ├── SettingsScreen.kt    # Settings UI
    │   └── SettingsViewModel.kt # Settings logic
    └── theme/
        ├── Color.kt         # Color definitions
        ├── Theme.kt         # Material3 theme setup
        └── Type.kt          # Typography definitions
```

## Architecture

- **UI Framework**: Jetpack Compose with Material3
- **Navigation**: Jetpack Navigation Compose with bottom navigation bar
- **Dependency Injection**: Koin
- **State Management**: ViewModel + StateFlow
- **Persistence**: DataStore Preferences

## Key Components

### MainActivity
Single activity that hosts the Compose UI. Uses edge-to-edge display.

### MainApplication
Initializes Koin with modules from:
- `AppModule` (this module)
- `CommonsModule` (shared utilities)
- `JestModule` (Jest sub-app)
- `WallmaticModule` (Wallmatic sub-app)

### Navigation
Two main screens accessible via bottom navigation:
- **Home** (`home` route): Displays sub-app grid
- **Settings** (`settings` route): App configuration

### Sub-Apps
Currently integrated sub-apps (defined in `HomeScreen.kt`):

| Name | Description | Status | Accent Color |
|------|-------------|--------|--------------|
| Jest | Daily humor/jokes | Active | `#FFD248` (Gold) |
| Wallmatic | Auto-change wallpapers | Active | `#4B46C0` (Purple) |
| Topty | 2FA authenticator | Coming Soon | `#2196F3` (Blue) |
| 2048 | Puzzle game | Coming Soon | `#E91E63` (Pink) |

### Theme System
- Supports Light, Dark, and System theme modes
- Dynamic color (Material You) support on Android 12+
- Configurable via Settings screen

### AppSettings (DataStore)
Persisted preferences:
- `theme_mode`: AppThemeMode enum (SYSTEM, LIGHT, DARK)
- `isDynamicTheme`: Boolean for Material You colors

## Dependencies

### Local Modules
- `projects.wallmatic` - Wallmatic sub-app
- `projects.jest` - Jest sub-app
- `projects.devButler` - Developer utilities
- `projects.topty` - Topty sub-app
- `projects.commons` - Shared utilities

### Key Libraries
- AndroidX Compose BOM
- Material3
- Navigation Compose
- DataStore Preferences
- Koin (DI)

## Build Configuration

- **Namespace**: `com.axondragonscale.appbox`
- **Min SDK**: 31 (Android 12)
- **Target SDK**: 36
- **Java/Kotlin**: 21

### Build Variants
- **Debug**: `.debug` suffix, signed with debug keystore
- **Release**: Minified with ProGuard

## Resources

### Launcher Icon
- Background: Purple gradient (`#6366F1` → `#4B46C0`)
- Foreground: 4 white rounded squares of varying sizes representing apps
- Files: `drawable/ic_launcher_background.xml`, `drawable/ic_launcher_foreground.xml`

## Adding a New Sub-App

1. Create the sub-app module with its own `Activity` and Koin module
2. Add the module dependency in `build.gradle.kts`
3. Register the Koin module in `MainApplication.kt`
4. Add entry to `subApps` list in `HomeScreen.kt`:
   ```kotlin
   SubApp(
       name = "AppName",
       description = "App description",
       icon = SubAppIcon.Drawable(R.drawable.app_icon),
       accentColor = Color(0xFFXXXXXX),
       enabled = true,
       onClick = { AppActivity.start(it) }
   )
   ```

## Code Style

- Compose UI with Material3 components
- ViewModels expose `StateFlow<UiState>`
- Settings use custom `setting()` and `enumSetting()` extensions from commons
- Previews provided for Light and Dark modes