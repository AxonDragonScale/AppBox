> Context: [Main](../../context.md) > Wallmatic

# Wallmatic - Wallpaper Auto-Changer App

## Overview

Wallmatic is an automatic wallpaper changer app within AppBox. It allows users to create albums of wallpapers from device folders and automatically cycles through them on a schedule. Supports separate configurations for home and lock screens, quick settings tiles, and boot-persistent scheduling.

- **Package**: `com.axondragonscale.wallmatic`
- **Module path**: `:apps:wallmatic`
- **Key Tech**: Room (persistence), Proto DataStore (settings), DocumentFile (SAF), AlarmManager (scheduling), Quick Settings Tiles

## Architecture

- **UI**: Jetpack Compose with Material3
- **Navigation**: Jetpack Navigation Compose (type-safe routes with `@Serializable`)
- **DI**: Koin (`WallmaticApplication` initializes `CommonsModule` + `WallmaticModule`)
- **State Management**: ViewModel + StateFlow
- **Persistence**: Room database + Proto DataStore
- **Background Work**: AlarmManager + BroadcastReceiver + Service — see [Background Services sub-context](./background-services.md) for details
- **Image Loading**: Coil

## Package Structure

```
com.axondragonscale.wallmatic/
├── WallmaticActivity.kt              # Entry point with edge-to-edge display
├── background/                       # See background-services.md for details
│   ├── HomeTileService.kt            # Quick settings tile (home screen)
│   ├── LockTileService.kt            # Quick settings tile (lock screen)
│   ├── WallmaticReceiver.kt          # Boot/time change broadcast receiver
│   ├── WallmaticScheduler.kt         # Alarm scheduling for auto-cycle
│   ├── WallmaticService.kt           # Background wallpaper update service
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

## WallmaticActivity

Entry point activity with edge-to-edge display. Syncs albums on start and applies theme preferences.

```kotlin
companion object {
    fun start(context: Context) {
        val intent = Intent(context, WallmaticActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
```

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
}

message WallpaperConfig {
    int32 albumId = 1;
    bool autoCycleEnabled = 2;
    int32 currentWallpaperId = 3;
    int64 updateInterval = 4;
    int64 lastUpdated = 5;
}
```

Key settings:
- **`mirrorHomeConfigForLock`**: When true, lock screen uses the same wallpaper as home screen
- **`autoCycleEnabled`**: Per-screen toggle for automatic wallpaper cycling
- **`updateInterval`**: Time between automatic wallpaper changes (milliseconds)

## Data Flow

### Album Creation
1. User creates album → inserted into Room
2. User adds folder → folder URI persisted, wallpapers scanned from filesystem
3. Wallpapers inserted into Room with folder reference
4. Album updated with folder/wallpaper IDs

### SyncManager
Synchronizes album contents with filesystem on app start:
- Detects added/removed wallpapers in folders
- Updates database to reflect current state
- Called automatically in `WallmaticActivity`

### Wallpaper Auto-Cycle (Summary)
1. `WallmaticScheduler` sets exact alarm for next update
2. Alarm triggers → `WallmaticReceiver` → `WallmaticService` → `WallpaperUpdater`
3. Random wallpaper selected from whitelisted pool → applied → next update scheduled

For full details on scheduling, tiles, receivers, and the update lifecycle, see [Background Services](./background-services.md).

## Permissions

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SET_WALLPAPER" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
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

- **Local Modules**: `projects.core.commons`
- **AndroidX**: Compose BOM + Material3, Navigation Compose, Room, Proto DataStore, DocumentFile
- **Koin**: DI
- **Coil**: Image loading
- **kotlinx**: serialization, coroutines
- **Protobuf**: protobuf-kotlin-lite

## Build Info

- **Namespace**: `com.axondragonscale.wallmatic`
- **Min SDK**: 31 (Android 12)
- **Java/Kotlin**: JVM 21

## Code Conventions

- ViewModels: `HomeVM`, `AlbumsVM`, `AlbumVM`, `FolderVM`, `WallpaperVM`, `SettingsVM`
- Type-safe navigation with `@Serializable` route classes
- Proto DataStore for complex configuration
- Manager classes for domain logic (`AlbumManager`, `FolderManager`, `SyncManager`, `WallpaperManager`)
- Extension functions for config manipulation (`homeConfig {}`, `lockConfig {}`)

## Related Context

- [Main Context](../../context.md) — Project overview, tech stack, architecture patterns
- [Commons Module](../../core/commons/context.md) — Shared utilities, networking, DI module
- [Coding Conventions](../../coding-conventions.md) — Compose patterns, Kotlin idioms, naming
- [Build System](../../build-system.md) — Convention plugins, version catalog
- **Sub-context:**
  - [Background Services](./background-services.md) — Scheduling, tiles, receivers, wallpaper update lifecycle