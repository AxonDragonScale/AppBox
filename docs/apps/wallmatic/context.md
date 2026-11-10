---
title: "Wallmatic — wallpaper auto-changer"
status: current
summary: "Cycles user-created wallpaper albums on a schedule, with separate home and lock configurations and quick settings tiles."
read_when: "Changing albums, config, scheduling, or the wallpaper apply path."
---

> Context: [Main](../../context.md) > Wallmatic

# Wallmatic - Wallpaper Auto-Changer App

## Overview

Wallmatic automatically cycles through user-created albums of wallpapers on a schedule. Supports separate configurations for home and lock screens, quick settings tiles, and scheduling that survives reboots.

- **Package**: `com.axondragonscale.wallmatic`
- **Module path**: `:apps:wallmatic`
- **Key Tech**: Room (persistence), Proto DataStore (settings), DocumentFile (SAF), AlarmManager + WorkManager (scheduling), Quick Settings Tiles

## Architecture

- **UI**: Jetpack Compose with Material3
- **Navigation**: Jetpack Navigation Compose (type-safe routes with `@Serializable`)
- **DI**: Koin (`WallmaticApplication` initializes `WallmaticModule`)
- **State Management**: ViewModel + StateFlow
- **Persistence**: Room database + Proto DataStore
- **Background Work**: `AlarmManager` tick + `TickReceiver` → `UpdateCoordinator`, with `WorkManager` for sync and a fallback net — see [Background Services sub-context](./background-services.md) for details
- **Image Loading**: Coil

## Package Structure

```
com.axondragonscale.wallmatic/
├── WallmaticActivity.kt              # Entry point with edge-to-edge display
├── background/                       # See background-services.md for details
│   ├── FallbackWorker.kt             # Periodic safety net: re-evaluates the rule
│   ├── HomeTileService.kt            # Quick settings tile (home screen)
│   ├── LockTileService.kt            # Quick settings tile (lock screen)
│   ├── SyncWorker.kt                 # Deferred album sync (WorkManager)
│   ├── TickReceiver.kt               # Tick alarm + boot/time/update broadcasts
│   ├── TickScheduler.kt              # The single AlarmManager tick
│   ├── UpdateCoordinator.kt          # The auto-cycle rule: apply if due, then arm
│   └── WallmaticTileService.kt       # Quick settings tile (both screens)
├── core/
│   ├── AlbumManager.kt               # Album CRUD operations
│   ├── FolderManager.kt              # Folder management
│   ├── SyncManager.kt                # Syncs albums with filesystem
│   ├── WallpaperManager.kt           # Wallpaper operations
│   └── WallpaperUpdater.kt           # Core wallpaper update logic
├── database/
│   ├── WallmaticDatabase.kt          # Room database definition
│   ├── converter/                    # Room type converters
│   ├── dao/                          # Data Access Objects
│   └── entity/
│       ├── Album.kt                  # Album entity
│       ├── Folder.kt                 # Folder entity
│       └── Wallpaper.kt              # Wallpaper entity
├── di/
│   └── WallmaticModule.kt            # Koin dependency injection module
├── model/
│   ├── FullAlbum.kt                  # Album with resolved folders/wallpapers
│   ├── FullFolder.kt                 # Folder with resolved wallpapers
│   ├── TargetScreen.kt               # Home/Lock/Both enum
│   └── UIMode.kt                     # Light/Dark/Auto theme mode
├── repository/
│   ├── AppPrefsRepository.kt         # Proto DataStore preferences
│   └── WallmaticRepository.kt        # Data layer operations
├── ui/
│   ├── Route.kt                      # Navigation routes (type-safe)
│   ├── WallmaticApp.kt               # Root composable with NavHost
│   ├── album/                        # Album detail screen
│   ├── albums/                       # Albums list screen
│   ├── bottombar/                    # Bottom navigation
│   ├── common/                       # Shared UI components
│   ├── dashboard/                    # Main dashboard with tabs
│   ├── folder/                       # Folder detail screen
│   ├── home/                         # Home tab (current wallpaper)
│   ├── settings/                     # Settings screen
│   ├── theme/                        # Wallmatic-specific theming
│   ├── util/                         # UI utilities
│   └── wallpaper/                    # Wallpaper detail/preview
└── util/
    └── DevTools.kt                   # Development utilities
```

## Navigation Routes

```kotlin
object Route {
    @Serializable data class Dashboard(val tab: Int = Tab.Home.position)
    @Serializable data class Album(val albumId: Int)
    @Serializable data class Folder(val folderId: Int)
    @Serializable data class Wallpaper(val wallpaperId: Int)
}
```

### Main Screens

- **Dashboard**: Tabbed container (Home, Albums, Settings)
- **Home**: Current wallpaper display, quick actions
- **Albums**: List of user-created albums
- **Album**: Album detail with folders and wallpapers
- **Folder**: Folder contents
- **Wallpaper**: Full-screen wallpaper preview
- **Settings**: App configuration

`WallmaticActivity` is the entry point. It syncs albums on start and applies theme preferences. Use `WallmaticActivity.start(context)` to launch it from outside.

## Database

### Entities

**Album**
```kotlin
@Entity(tableName = "album")
data class Album(
    var name: String,
    var coverUri: String? = null,
    var wallpaperIds: List<Int> = emptyList(),
    var folderIds: List<Int> = emptyList(),
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
```

**Folder**: Contains URI reference to device folder, list of wallpaper IDs

**Wallpaper**: Individual wallpaper with URI, parent folder ID, whitelist status

### Models

- `FullAlbum`: Album with its resolved folders and wallpapers (joined data)
- `FullFolder`: Folder with its resolved wallpapers (joined data)
- `TargetScreen`: Enum — `Home`, `Lock`, `Both`

## Configuration (Proto DataStore)

```protobuf
message Config {
  bool isInit = 1;
  bool mirrorHomeConfigForLock = 2;
  WallpaperConfig homeConfig = 3;
  WallpaperConfig lockConfig = 4;
  int64 updateInterval = 5;  // Shared interval for both screens (millis)
  int64 lastUpdated = 6;     // Shared last update timestamp (epoch millis)
}

message WallpaperConfig {
  int32 albumId = 1;
  bool autoCycleEnabled = 2;
  int32 currentWallpaperId = 3;
  // field 4 retired (was updateInterval) — do not reuse
  // field 5 retired (was lastUpdated) — do not reuse
}
```

Key settings:
- **`mirrorHomeConfigForLock`**: When true, lock screen uses the same wallpaper as home screen
- **`autoCycleEnabled`**: Per-screen toggle for automatic wallpaper cycling
- **`updateInterval`**: Single shared interval for both screens (milliseconds)
- **`lastUpdated`**: Shared timestamp — used with `updateInterval` to compute the next update time

## Data Flow

### Album Creation
1. User creates album → inserted into Room
2. User adds folder → folder URI persisted, wallpapers scanned from filesystem
3. Wallpapers inserted into Room with folder reference
4. Album updated with folder/wallpaper IDs

### Wallpaper Auto-Cycle (Summary)
1. `TickScheduler` keeps exactly one `AlarmManager` tick armed at `config.nextUpdate` (`lastUpdated + updateInterval`)
2. `TickReceiver` handles the tick plus `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` and `MY_PACKAGE_REPLACED`, and forwards all of them to `UpdateCoordinator.reconcile()`
3. `reconcile()` applies the wallpaper if due (`WallpaperUpdater.updateWallpaper()`) and re-arms the tick from the new `lastUpdated` — or, when an apply failed, a bounded 1–5 minute retry
4. `FallbackWorker` re-evaluates the same rule every 15 minutes in case the alarm was dropped; `SyncWorker` refreshes albums off the timed path; `WallmaticActivity.onStart` reconciles on every return to the foreground

For full details on scheduling, workers, tiles, and the update lifecycle, see [Background Services](./background-services.md).

## Permissions

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SET_WALLPAPER" />
```

## Resources

### Launcher Icon
- Accent Color: `#4B46C0` (Purple/Indigo)
- Icon files in `mipmap-*/wallmatic_icon*`

### Quick Settings Icons
- `ic_replace_image`: Both wallpapers
- `ic_home_replace_image`: Home screen
- `ic_lock_replace_image`: Lock screen

## Dependencies

- **Local Modules**: `:core:common`, `:core:ui`, `:core:devtools`, `:core:room`
- **AndroidX**: Compose BOM + Material3, Navigation Compose, Room, Proto DataStore, DocumentFile, WorkManager
- **Koin**: DI (with `koin-androidx-workmanager` for worker injection)
- **Coil**: Image loading
- **kotlinx**: serialization, coroutines
- **Protobuf**: protobuf-kotlin-lite

## Build Info

- **Namespace**: `com.axondragonscale.wallmatic`
- **Min SDK**: 31 (Android 12)
- **Java/Kotlin**: JVM 21

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Core Modules](../../core/context.md) — Shared utilities, networking, DI module
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
- **Sub-context:**
  - [Background Services](./background-services.md) — Scheduling, tiles, receivers, wallpaper update lifecycle
