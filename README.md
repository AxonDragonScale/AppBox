# AppBox

A monorepo containing a collection of Android applications and shared utilities.

## Project Structure

```
AppBox/
├── apps/                    # Standalone Android applications
│   ├── jest/               # Daily humor/jokes app
│   ├── wallmatic/          # Auto-change wallpapers app
│   └── topty/              # 2FA authenticator app
├── core/                    # Shared library modules
│   └── commons/            # Common utilities, networking, UI components
├── build-logic/            # Gradle convention plugins
├── keystore/               # Signing configuration
└── gradle/                 # Version catalog and Gradle wrapper
```

## Apps

### Jest
Get your daily dose of humor with curated jokes and fun content.
- **Package**: `com.axondragonscale.jest`
- **Features**: Daily jokes, favorites, history, home screen widget

### Wallmatic
Automatically changes your wallpaper with time. Set it and enjoy fresh backgrounds throughout the day.
- **Package**: `com.axondragonscale.wallmatic`
- **Features**: Scheduled wallpaper changes, album management, quick settings tiles

### Topty
A lightweight authenticator app for managing your 2FA codes securely and efficiently.
- **Package**: `com.axondragonscale.topty`
- **Features**: TOTP code generation, QR code scanning, secure storage

## Building

Each app can be built independently:

```bash
# Build all apps
./gradlew assembleDebug

# Build specific app
./gradlew :apps:jest:jestAndroid:assembleDebug
./gradlew :apps:wallmatic:assembleDebug
./gradlew :apps:topty:assembleDebug
```

## Architecture

- **UI Framework**: Jetpack Compose with Material3
- **Dependency Injection**: Koin
- **Networking**: Ktor
- **Database**: Room
- **State Management**: ViewModel + StateFlow
- **Build System**: Gradle with convention plugins

## Tech Stack

| Category | Libraries |
|----------|-----------|
| UI | Jetpack Compose, Material3, Coil |
| DI | Koin |
| Networking | Ktor, Kotlin Serialization |
| Database | Room |
| Async | Kotlin Coroutines, Flow |
| Widgets | Glance |

## Requirements

- **Min SDK**: 31 (Android 12)
- **Target SDK**: 36
- **Java/Kotlin**: 21

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests for new features, bug fixes, or improvements.

## License

This project is for personal use. License details to be added.