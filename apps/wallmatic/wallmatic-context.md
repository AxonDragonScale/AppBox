# Wallmatic - Module Context

## Overview

Wallmatic is an automatic wallpaper changer sub-app within AppBox. It allows users to create albums of wallpapers from device folders and automatically cycles through them on a schedule. Supports separate configurations for home and lock screens, quick settings tiles, and boot-persistent scheduling.

## Package Structure

```
com.axondragonscale.wallmatic/
├── WallmaticActivity.kt              # Entry point activity
├── background/
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

## Architecture

- **UI Framework**: Jetpack Compose with Material3
- **Navigation**: Jetpack Navigation Compose (type-safe routes)
- **Dependency Injection**: Koin
- **State Management**: ViewModel + StateFlow
- **Persistence**: Room database + Proto DataStore
- **Background Work**: AlarmManager + BroadcastReceiver + Service
- **Image Loading**: Coil

## Key Components

### WallmaticActivity
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

### Navigation Routes
Type-safe navigation using kotlinx.serialization:
```kotlin
object Route {
    data class Dashboard(val tab: Int = Tab.Home.position)
    data class Album(val albumId: Int)
    data class Folder(val folderId: Int)
    data class Wallpaper(val wallpaperId: Int)
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

### Database Entities

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

### Configuration (Proto DataStore)

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

### WallpaperUpdater
Core logic for changing wallpapers:
- Checks if update interval has passed
- Supports separate home/lock configurations
- Mirroring option (same wallpaper for both)
- Selects random wallpaper from whitelisted pool
- Uses `WallpaperManager.setStream()` with appropriate flags
- Schedules next update after completion

### Background Services

**WallmaticScheduler**: Schedules exact alarms for auto-cycle updates

**WallmaticReceiver**: Handles boot completion, time/timezone changes to reschedule alarms

**WallmaticService**: Foreground service for wallpaper updates

**Quick Settings Tiles**:
- `WallmaticTileService`: Change both wallpapers
- `HomeTileService`: Change home screen only
- `LockTileService`: Change lock screen only

### SyncManager
Synchronizes album contents with filesystem:
- Detects added/removed wallpapers in folders
- Updates database to reflect current state
- Called on app start

## Dependencies

### Local Modules
- `projects.commons` - Shared utilities

### Key Libraries
- AndroidX Compose BOM + Material3
- Navigation Compose
- Coil (image loading)
- Koin (DI)
- Room (database)
- Proto DataStore (typed preferences)
- DocumentFile (SAF support)
- kotlinx.serialization

## Build Configuration

- **Namespace**: `com.axondragonscale.wallmatic`
- **Min SDK**: 31 (Android 12)
- **Java/Kotlin**: 21
- **Protobuf**: Uses protobuf-kotlin-lite

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

## Data Flow

### Album Creation
1. User creates album → inserted into Room
2. User adds folder → folder URI persisted, wallpapers scanned
3. Wallpapers inserted into Room with folder reference
4. Album updated with folder/wallpaper IDs

### Wallpaper Auto-Cycle
1. `WallmaticScheduler` sets exact alarm for next update
2. Alarm triggers → `WallmaticReceiver` receives broadcast
3. Receiver starts `WallmaticService`
4. Service calls `WallpaperUpdater.updateWallpaper()`
5. Updater checks config, selects random wallpaper
6. `WallpaperManager.setStream()` applies wallpaper
7. Config updated with new wallpaper ID and timestamp
8. Next update scheduled

### Quick Settings Tile
1. User taps tile → `TileService.onClick()`
2. Service calls `WallpaperUpdater.updateWallpaper(target)`
3. Wallpaper changed immediately
4. Next scheduled update remains unchanged

## Code Style

- ViewModels: `HomeVM`, `AlbumsVM`, `AlbumVM`, `FolderVM`, `WallpaperVM`, `SettingsVM`
- Type-safe navigation with `@Serializable` route classes
- Proto DataStore for complex configuration
- Manager classes for domain logic (`AlbumManager`, `FolderManager`, etc.)
- Extension functions for config manipulation (`homeConfig {}`, `lockConfig {}`)