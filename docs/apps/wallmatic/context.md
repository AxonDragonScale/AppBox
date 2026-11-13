---
title: "Wallmatic — wallpaper auto-changer"
status: current
summary: "Cycles user-created wallpaper albums on a schedule, with separate home and lock configurations, per-wallpaper crops, and quick settings tiles."
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
│   ├── WallpaperCrop.kt              # Crop resolver: viewport()
│   ├── WallpaperCropTransformation.kt # Coil transformation that crops a preview
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
│   ├── Interval.kt                   # Tick list, bounds, labels and coercion
│   ├── TargetScreen.kt               # Home/Lock/Both enum
│   ├── UIMode.kt                     # Light/Dark/Auto theme mode
│   └── WallpaperCrop.kt              # Focal point + zoom, and the Wallpaper.crop helper
├── repository/
│   ├── AppPrefsRepository.kt         # Proto DataStore preferences
│   └── WallmaticRepository.kt        # Data layer operations
├── ui/
│   ├── Route.kt                      # Navigation routes (type-safe)
│   ├── WallmaticApp.kt               # Root composable with NavHost
│   ├── album/                        # Album detail screen
│   ├── albums/                       # Library (album list) screen
│   ├── bottombar/                    # Tab definitions
│   ├── component/                    # Paper/ink component kit (see ui.md)
│   ├── dashboard/                    # Main dashboard with tabs
│   ├── folder/                       # Folder detail screen
│   ├── home/                         # Home tab (current wallpaper)
│   ├── settings/                     # Settings screen
│   ├── theme/                        # Wallmatic-specific theming
│   ├── util/                         # UI utilities
│   └── wallpaper/                    # Wallpaper viewer and crop editor (one screen, two modes)
└── util/
    ├── ConfigUtils.kt                # Config derivation: active targets, activation, failures
    └── DevTools.kt                   # Development utilities
```

## Navigation Routes

```kotlin
object Route {
    @Serializable data class Dashboard(val tab: Int = Tab.Home.position)
    @Serializable data class Album(val albumId: Int)
    @Serializable data class Folder(val folderId: Int)
    @Serializable data class Wallpaper(val wallpaperId: Int, val edit: Boolean = false)
}
```

### Main Screens

- **Dashboard**: Tabbed container (Home, Library, Settings)
- **Home**: The active album's current wallpaper, the Home/Lock switcher, schedule and shared interval, album selection, and the failure row
- **Library**: Album grid with target badges and the create action
- **Album**: Album detail with folders and wallpapers
- **Folder**: Folder contents
- **Wallpaper**: One full-screen screen with two modes. View mode shows the full-bleed image with the
  stored crop and a chrome that carries `ADJUST`; edit mode is the crop editor — draft focal point and
  zoom, `CENTER` and `RESET` — switched to in place, or entered directly with
  `Route.Wallpaper(edit = true)` from the long-press `Adjust` row
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

**Wallpaper**: Individual wallpaper with URI, whitelist status, and the crop fields `cropFocalX` / `cropFocalY` (fractions of the source, default `0.5f`) and `cropZoom` (default `1f`). `hasCustomCrop` is computed — true once any field leaves the default — and nothing stores it.

`WallmaticDatabase` is at version 2. `MIGRATION_1_2` is handwritten and registered on the builder: three `ALTER TABLE wallpaper ADD COLUMN` statements adding `cropFocalX` and `cropFocalY` as `REAL NOT NULL DEFAULT 0.5` and `cropZoom` as `REAL NOT NULL DEFAULT 1.0`. There is no destructive fallback, so an older install opened without this migration would fail to open the database rather than lose its rows.

### Models

- `FullAlbum`: Album with its resolved folders and wallpapers (joined data)
- `FullFolder`: Folder with its resolved wallpapers (joined data)
- `TargetScreen`: Enum — `Home`, `Lock`, `Both`
- `WallpaperCrop`: Focal point plus zoom; `Wallpaper.crop` returns it, or null while the row is at the default centre crop

## Wallpaper crop

A wallpaper's crop is a focal point plus a zoom, stored on its row. The default — `0.5 / 0.5` at
`1x` — is the centre crop the app has always applied and drawn; `hasCustomCrop` selects the
crop-aware paths below. Because the crop lives on the row, the same file added to two folders
carries two independent crops.

### Resolver

`apps/wallmatic/src/main/java/com/axondragonscale/wallmatic/core/WallpaperCrop.kt` holds one pure
resolver:

```kotlin
fun viewport(sourceWidth, sourceHeight, focalX, focalY, zoom, targetAspect): Rect
```

It takes the largest rect at `targetAspect` that fits the source, divides it by `zoom` (clamped
1–4), centres it on the focal point and clamps it inside the source, rounding outward so the rect
covers the region asked for. At default values it returns the centre crop. The apply path, the
editor and the preview transformation all resolve through it.

### Apply

`WallpaperUpdater.setWallpaper()` is `suspend` and branches on `hasCustomCrop`:

- **No custom crop** — the wallpaper is streamed untouched with
  `setStream(stream, null, true, flags)`, so nothing is decoded and the system performs its own
  crop.
- **Custom crop** — on `Dispatchers.IO`: probe the stored bounds with `BitmapFactory` in
  `inJustDecodeBounds` mode, choose a decode size that puts the crop region on the target at 1:1,
  decode through Coil (`Precision.INEXACT`, hardware bitmaps off, memory and disk caches disabled),
  crop with `viewport` and scale into an `ARGB_8888` bitmap at the target size with a filtering
  `Canvas` draw, then apply with `setBitmap`. The decode budget is a quarter of
  `Runtime.getRuntime().maxMemory()`, clamped to 32–64 MB, with a 4096 px absolute dimension guard,
  so a 12 MP photo at 1x is not downsampled below the crop region. Any failure logs and falls back
  to the stream path, so the wallpaper is still set.
- The cropped output is screen-sized by design: the target is
  `WindowManager.currentWindowMetrics.bounds`, which includes the system bars, so the editor's frame
  and the applied wallpaper share one aspect. A launcher that pans a wider virtual desktop may shift
  the framing — accepted rather than compensated for. A higher zoom can upscale the crop region to
  the target — accepted rather than refused.

### Previews

`WallpaperCropTransformation` (cache key `WallpaperCrop:<focalX>:<focalY>:<zoom>`) resolves the
rect against each request's own target size, so the Home preview, the 178 × 395 tile and the
full-bleed viewer show the same region at their own sizes. `wallpaperModel(uri, crop)` wraps the
URI in an `ImageRequest` carrying that transformation, and returns the plain URI when the crop is
null. `WallpaperPlate` and `WallpaperTile` take an optional `WallpaperCrop`; the album and folder
grids, the Home preview, the action overlay and the full-screen viewer pass it. Album covers resolve
their crop by URI from `WallmaticRepository.getCoverCrops()` — a live `Flow` of the non-default
crops, so a crop edit repaints any visible card, row or picker entry in place; a cover URI with no
matching wallpaper row falls back to the centre crop, and when two rows share a URI the lowest id
wins. Folder strips pass each wallpaper's own `crop`, which the album screen's
`getWallpapersFlow()` keeps live.

### Viewer and editor

`Route.Wallpaper(wallpaperId, edit = false)` opens `ui/wallpaper/`: `Wallpaper.kt`, `WallpaperVM`,
`WallpaperUiState` and `WallpaperUiEvent`. One VM and one state cover both modes: `isLoading`, the
loaded `wallpaper`, `isMissing`, `mode` (`View` / `Edit`), the draft `focalX` / `focalY` / `zoom`,
`isSaving`, and a computed `isDirty` that compares the draft with the stored crop. `edit = true`
selects `Edit` at construction; otherwise the mode changes in place and never navigates.

View mode draws the stored crop full-bleed with the chrome hidden until a tap. Edit mode draws the
draft crop full-bleed: pinch and drag move the draft, `CENTER` returns the focal point to
`0.5 / 0.5` and keeps the zoom, and `RESET` restores both. `DONE` is enabled only while `isDirty`; it
saves the crop as a single `UPDATE wallpaper SET cropFocalX / cropFocalY / cropZoom WHERE id`, so the
row is never duplicated, then returns the screen to view mode rather than popping. Back in edit mode
discards the draft and returns to view; back in view mode pops.

The editor is reached from `ADJUST` in view mode and from `Adjust` in a wallpaper's long-press menu.
After the update, the VM reloads the row and calls `UpdateCoordinator.reapply()`: the crop is always
written, and the wallpaper is re-applied only if it is already showing on Home or Lock — see
[Background Services](./background-services.md#saving-a-crop).

The album and folder screens observe the `wallpaper` table through `WallmaticDao.getWallpapersFlow()`,
keyed by their wallpaper ids, so a changed row — a crop included — repaints the grid and the
folder strips immediately without a reload.

## Configuration (Proto DataStore)

```protobuf
message Config {
  bool isInit = 1;
  int64 updateInterval = 2;  // Shared interval for both screens (millis)
  int64 lastUpdated = 3;     // Shared last update timestamp (epoch millis)
  bool mirrorHomeConfigForLock = 4;
  WallpaperConfig homeConfig = 5;
  WallpaperConfig lockConfig = 6;
  int64 lastFailedAt = 7;    // Last failed apply; -1 after a clean update
  int32 failedTarget = 8;    // Target that failed last: 0 Home, 1 Lock, 2 Both
}

message WallpaperConfig {
  int32 albumId = 1;
  bool autoCycleEnabled = 2;
  int32 currentWallpaperId = 3;
}
```

Key settings:
- **`mirrorHomeConfigForLock`**: When true, lock screen uses the same wallpaper as home screen
- **`autoCycleEnabled`**: Per-screen flag; selecting an album sets it, and the Home design has no pause control
- **`updateInterval`**: Single shared interval for both screens (milliseconds)
- **`lastUpdated`**: Shared timestamp — used with `updateInterval` to compute the next update time
- **`lastFailedAt`**: Written by `WallpaperUpdater` on a failed apply, cleared on success; drives the
  `LAST UPDATE FAILED` row

## Data Flow

### Album Creation
1. User creates album → inserted into Room
2. User adds folder → folder URI persisted, wallpapers scanned from filesystem
3. Wallpapers inserted into Room with folder reference
4. Album updated with folder/wallpaper IDs

### Manual Set
`Set Home` / `Set Lock` / `Set Both` from the wallpaper menu calls
`UpdateCoordinator.applyWallpaper()`: the chosen row is applied immediately, `currentWallpaperId` is
written for the applied screens (`Both` writes both) and `lastUpdated` advances, so the Home preview
and `CURRENT` follow the change. The schedule re-arms from the new timestamp.

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
