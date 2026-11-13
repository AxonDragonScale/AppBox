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
- **Key Tech**: Room (persistence), Proto + Preferences DataStore (settings), DocumentFile (SAF), AlarmManager + WorkManager (scheduling), Quick Settings Tiles

## Architecture

- **UI**: Jetpack Compose with Material3
- **Navigation**: Jetpack Navigation Compose (type-safe routes with `@Serializable`)
- **DI**: Koin (`WallmaticApplication` initializes `WallmaticModule`)
- **State Management**: ViewModel + StateFlow
- **Persistence**: normalised Room database; Proto DataStore for the cycle config; Preferences DataStore for appearance and grid size
- **Background Work**: `AlarmManager` tick + `TickReceiver` → `UpdateCoordinator` → `WallpaperCycler` → `WallpaperApplier`, with `WorkManager` for sync and a fallback net — see [Background Services sub-context](./background-services.md) for details
- **Image Loading**: Coil

## Package Structure

```
com.axondragonscale.wallmatic/
├── WallmaticActivity.kt              # Entry point with edge-to-edge display
├── WallmaticApplication.kt           # Koin startup
├── background/                       # See background-services.md for details
│   ├── FallbackWorker.kt             # Periodic safety net: re-evaluates the rule
│   ├── SyncWorker.kt                 # Deferred album sync + config reconcile (WorkManager)
│   ├── TickReceiver.kt               # Tick alarm + boot/time/update broadcasts
│   ├── TickScheduler.kt              # The single AlarmManager tick
│   ├── TileServices.kt               # Shared tile base + the three Quick Settings tiles
│   └── UpdateCoordinator.kt          # Mutex + alarm/worker arming around the cycler
├── core/
│   ├── AlbumEditor.kt                # User-driven folder/wallpaper adds and removes
│   ├── AlbumSyncer.kt                # Reconciles album rows with the filesystem
│   ├── ConfigReconciler.kt           # Reconciles config with albums, re-applies stale wallpapers
│   ├── CropGeometry.kt               # cropBox()/viewport(): the one crop resolver
│   ├── WallpaperApplier.kt           # Decode, crop and hand off to WallpaperManager
│   ├── WallpaperCropTransformation.kt # Coil transformation that crops a preview
│   └── WallpaperCycler.kt            # Selection, persistence and the cadence rule
├── database/
│   ├── WallmaticDatabase.kt          # Room database definition (version 3)
│   ├── dao/                          # Data Access Objects
│   └── entity/
│       ├── Album.kt                  # Album entity
│       ├── Folder.kt                 # Folder entity (owns its album)
│       └── Wallpaper.kt              # Wallpaper entity (owns album, optional folder, crop)
├── di/
│   └── WallmaticModule.kt            # Koin dependency injection module
├── model/
│   ├── FullAlbum.kt                  # Album with resolved folders/wallpapers and a derived cover
│   ├── FullFolder.kt                 # Folder with resolved wallpapers and a derived cover
│   ├── Interval.kt                   # Tick list, bounds, labels and coercion
│   ├── TargetScreen.kt               # Home/Lock/Both enum
│   ├── WallmaticConfig.kt            # Domain config: interval, screens, mirroring, failure
│   ├── WallpaperCrop.kt              # Focal point + zoom value object
│   └── WallpaperUpdateResult.kt      # Updated / NotDue / Skipped / Failed
├── repository/
│   ├── ConfigRepository.kt           # Proto DataStore config + serializer
│   ├── PreferencesRepository.kt      # Preferences DataStore (core:datastore): uiMode, dynamicTheme, gridSize
│   └── WallmaticRepository.kt        # Data layer operations
├── ui/
│   ├── Route.kt                      # Navigation routes (type-safe)
│   ├── WallmaticApp.kt               # Root composable with NavHost
│   ├── album/                        # Album detail screen
│   ├── bottombar/                    # Tab definitions
│   ├── component/                    # Paper/ink component kit (see ui.md)
│   ├── dashboard/                    # Main dashboard with tabs
│   ├── devtools/                     # Wallmatic-specific Dev Tools section
│   ├── folder/                       # Folder detail screen
│   ├── home/                         # Home tab (current wallpaper)
│   ├── library/                      # Library (album list) screen
│   ├── settings/                     # Settings screen
│   ├── theme/                        # Wallmatic-specific theming
│   ├── util/                         # UI utilities
│   └── wallpaper/                    # Wallpaper viewer and crop editor (one screen, two modes)
└── util/
    ├── ConfigMapping.kt              # Proto <-> WallmaticConfig mapping
    ├── ContextExt.kt                 # Persistable URI permission helper
    ├── FileUtil.kt                   # SAF folder scanning and file validation
    └── WallmaticConstants.kt         # Shared constants
```

## Navigation Routes

```kotlin
object Route {
    @Serializable data class Dashboard(val tab: String = Tab.Home.name)
    @Serializable data class Album(val albumId: Int)
    @Serializable data class Folder(val folderId: Int)
    @Serializable data class Wallpaper(val wallpaperId: Int, val edit: Boolean = false)
}
```

### Main Screens

- **Dashboard**: Tabbed container (Home, Library, Settings)
- **Home**: The active album's current wallpaper, the Home/Lock switcher, schedule and shared interval, album selection, and the failure row
- **Library**: Album grid with target badges and the create action; the first-run empty state is the
  shared `EmptyAlbumScreen`, also used by Home
- **Album**: Album detail with folders and wallpapers
- **Folder**: Folder contents
- **Wallpaper**: One full-screen screen with two modes. View mode shows the full-bleed image with the
  stored crop and a chrome that carries `ADJUST`; edit mode is the crop editor — draft focal point and
  zoom, `CENTER` and `RESET` — switched to in place, or entered directly with
  `Route.Wallpaper(edit = true)` from the long-press `Adjust` row
- **Settings**: App configuration

`WallmaticActivity` is the entry point. It syncs albums and reconciles the config on start, applies
the cycle if due on every `onStart`, and applies the theme preferences.

## Database

### Entities

Ownership is a foreign key on the child: a folder belongs to an album, and a wallpaper belongs to
an album and optionally to a folder. There are no id lists and no stored covers — a cover is derived
from the first folder or wallpaper.

**Album**
```kotlin
@Entity(tableName = "album")
data class Album(
    val name: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
```

**Folder**: `name`, `folderUri` (the persisted SAF reference) and `albumId`.

**Wallpaper**: `uri`, `albumId`, an optional `folderId` (`null` for a wallpaper added directly to the
album), `isBlacklisted`, and the `@Embedded` crop — `focalX` / `focalY` (fractions of the source,
default `0.5f`) and `zoom` (default `1f`). `hasCustomCrop` is computed from `crop.isDefault`; nothing
stores it.

`WallmaticDatabase` is at version 3, built with
`.fallbackToDestructiveMigration(dropAllTables = true)`. There is no handwritten migration and no
`@TypeConverters`: the normalised schema has no list columns to convert, so an older install is
dropped and recreated rather than migrated.

### Models

- `FullAlbum`: Album with its resolved folders and direct wallpapers (assembled from the three
  tables); `cover` is the first folder's cover or else the first direct wallpaper, and
  `allWallpapers` is the direct rows plus every folder's rows
- `FullFolder`: Folder with its resolved wallpapers; `cover` is the first wallpaper
- `TargetScreen`: Enum — `Home`, `Lock`, `Both`
- `WallpaperCrop`: Focal point plus zoom value object, embedded on `Wallpaper` and reused as the
  editor's draft; `isDefault` marks the centre `1x` crop
- `WallmaticConfig`: The domain config — `interval`, `lastUpdated`, `mirrorHomeForLock`, a
  `ScreenConfig` for Home and Lock (`albumId` and `currentWallpaperId`, both nullable), and an
  optional `WallpaperFailure` (`target`, `at`). It owns `isInitialised`, `nextUpdate`, target and
  current-id resolution (`albumIdFor`, `currentWallpaperIdFor`), activation, `clearHome()` /
  `clearLock()` and failure clearing, so no caller derives those from raw ids

## Core services

- `WallpaperApplier` — the Android half of an apply: decode, crop, then `setBitmap` or `setStream`.
  It has no config or repository dependency; `applySize()` and `sourceSize()` are public for Dev
  Tools.
- `WallpaperCycler` — selection, persistence and cadence: `cycleIfDue()`, `cycle(target)`,
  `applyWallpaper(wallpaper, target)` and `reapply(wallpaper)`. It picks a random non-blacklisted
  wallpaper per screen and records the outcome through `WallmaticConfig.recordApplied`, or
  `recordFailure` when nothing applied. One `effectiveTarget` rule covers mirroring: every apply and
  record is `Both`, including a manual `Set Home`.
- `UpdateCoordinator` — only the mutex and the alarm/worker arming. Every trigger (`applyIfDue`,
  `applyNow`, `applyWallpaper`, `reapply`, `albumsChanged`, `refresh`, `reschedule`) converges on
  the same lock; `refresh` runs the SAF sync outside it, then reconciles under it.
- `AlbumEditor` — user-driven adds and removes: `addFolder`, `addWallpapers`, `removeFolder`. It
  takes the SAF permission and skips duplicates; it never rewrites covers.
- `AlbumSyncer` — filesystem reconciliation only (`syncAlbums()`), with no config writes.
- `ConfigReconciler` — reconciles config with the albums that still exist (`reconcile()`), never a
  SAF scan. It reconciles each configured screen: a missing or empty album clears that screen through
  `clearHome()` / `clearLock()`, and a screen whose current wallpaper is no longer in its album is
  re-applied. When both screens reconcile, a `Failed` result wins so the coordinator arms the
  bounded retry.

## Wallpaper crop

A wallpaper's crop is a focal point plus a zoom, stored on its row as an embedded `WallpaperCrop`.
The default — `0.5 / 0.5` at `1x` — is the centre crop the app has always applied and drawn;
`hasCustomCrop` selects the crop-aware paths below. Because the crop lives on the row, the same file
added to two folders carries two independent crops.

### Resolver

`apps/wallmatic/src/main/java/com/axondragonscale/wallmatic/core/CropGeometry.kt` holds the one
crop resolver, in fractions and in pixels:

```kotlin
fun cropBox(sourceAspect, targetAspect, crop: WallpaperCrop): CropBox
fun viewport(sourceWidth, sourceHeight, crop: WallpaperCrop, targetAspect): Rect
```

`cropBox` takes the largest rect at `targetAspect` that fits the source, divides it by `zoom`
(clamped 1–4), centres it on the focal point and clamps it inside the source. `viewport` is the same
rect in source pixels, rounded outward so it covers the region asked for. At default values both
return the centre crop. The apply path, the editor and the preview transformation all resolve
through `cropBox`/`viewport`.

### Apply

`WallpaperApplier.apply(wallpaper, target)` is `suspend`, returns a `Result` and branches on
`hasCustomCrop`:

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
the default. `WallpaperPlate` and `WallpaperTile` take a non-null `WallpaperCrop`; the album and
folder grids, the Home preview, the action overlay and the full-screen viewer pass the row's own
`crop`. Covers are derived, not stored: `AlbumCard` and `AlbumRow` take the album's
`cover: Wallpaper?` and pass its `uri` and `crop`, so an album with no wallpaper rows shows the empty
mark. `FullAlbum` and `FullFolder` are observed whole, so a crop edit repaints any visible card, row
or picker entry in place; folder strips pass each wallpaper's own `crop` from the same observed
`FullFolder`.

### Viewer and editor

`Route.Wallpaper(wallpaperId, edit = false)` opens `ui/wallpaper/`: `Wallpaper.kt`, `WallpaperVM`,
`WallpaperUiState` and `WallpaperUiEvent`. One VM and one state cover both modes: `isLoading`, the
loaded `wallpaper` (null when the row is missing — the screen checks for null rather than a separate
flag), `mode` (`View` / `Edit`), the draft `draftCrop: WallpaperCrop`, `isSaving`, and a computed
`isDirty` that compares the draft with the stored crop. `edit = true` selects `Edit` at construction;
otherwise the mode changes in place and never navigates.

View mode draws the stored crop full-bleed with the chrome hidden until a tap. Edit mode draws the
draft crop full-bleed: pinch and drag move the draft, `CENTER` returns the focal point to
`0.5 / 0.5` and keeps the zoom, and `RESET` restores both. `DONE` is enabled only while `isDirty`; it
saves the crop as a single `UPDATE wallpaper SET focalX / focalY / zoom WHERE id`, so the row is
never duplicated, then returns the screen to view mode rather than popping. Back in edit mode
discards the draft and returns to view; back in view mode pops.

The editor is reached from `ADJUST` in view mode and from `Adjust` in a wallpaper's long-press menu.
After the update, the VM copies the saved crop into its state (no re-read) and calls
`UpdateCoordinator.reapply()`: the crop is always written, and the wallpaper is re-applied only if it
is already showing on Home or Lock — see
[Background Services](./background-services.md#saving-a-crop).

The album and folder screens observe `WallmaticRepository.observeFullAlbums()` /
`observeFullFolders()`, so a changed row — a crop included — repaints the grid and the folder strips
immediately without a reload.

## Configuration (Proto DataStore)

The domain type is `WallmaticConfig`; the generated protobuf `Config` exists only inside
`apps/wallmatic/src/main/java/com/axondragonscale/wallmatic/util/ConfigMapping.kt` and the DataStore
serializer. `ConfigRepository` is the only config read/write path — `configFlow` maps proto
to domain, and `update {}` is a read-modify-write against the latest stored document. The store is
the file `wallmatic_config_store_v2`; the `_v2` name sidesteps the old proto field layout.

```protobuf
message Config {
  int64 updateInterval = 1;  // Shared interval for both screens (millis)
  int64 lastUpdated = 2;     // Shared last update timestamp (epoch millis)
  bool mirrorHomeForLock = 3;
  ScreenConfig home = 4;
  ScreenConfig lock = 5;
  Failure failure = 6;
}

message ScreenConfig {
  int32 albumId = 1;
  int32 currentWallpaperId = 2;
}

message Failure {
  Screen target = 1;
  int64 at = 2;
}

enum Screen {
  SCREEN_UNSPECIFIED = 0;
  SCREEN_HOME = 1;
  SCREEN_LOCK = 2;
  SCREEN_BOTH = 3;
}
```

Key settings:
- **`interval`** (`updateInterval` in the proto): one shared `Duration` for both screens, defaulting
  to `Interval.default` (15 minutes) when the stored value is zero
- **`lastUpdated`**: shared `Instant?` — used with `interval` to compute `nextUpdate`; zero in the
  proto maps to `null`
- **`mirrorHomeForLock`**: when true, Lock follows Home — `albumIdFor(Lock)` and the current
  wallpaper both resolve to Home's
- **`home` / `lock`**: a `ScreenConfig` each, with nullable `albumId` and `currentWallpaperId`. Zero
  in the proto is the storage sentinel for `null`; the domain has no `-1`/`0` sentinels and no
  `isInit` flag. `isInitialised` is true when Home is set, or when mirroring is off and Lock is set,
  so a Lock-only config schedules and cycles; `albumIdFor(Both)` and `currentWallpaperIdFor(Both)`
  fall back to Lock
- **`failure`**: the last failed apply as `target` + `at`, cleared by a successful apply that
  resolves that target; it drives the `LAST UPDATE FAILED` row and the coordinator's early retry

`PreferencesRepository` owns the separate Preferences DataStore through `:core:datastore`'s
`createPreferencesDataStore` / `pref` / `enumPref` helpers, so it takes no `Context`. It exposes the
three public preference handles — `uiMode` (stored by enum name), `dynamicTheme` and
`gridSize` — and the screens collect and set them directly. There is no `fastAutoCycle`
preference; the Dev Tools toggle writes a 1-minute interval through `ConfigRepository` and restores
`Interval.default` when turned off.

## Data Flow

### Album Creation
1. User creates album → a row in `album`
2. User adds a folder → the SAF permission is persisted and `saveFolderWithWallpapers` inserts the
   folder with its `albumId` and its scanned wallpapers with that `folderId` in one transaction
3. User adds images directly → wallpapers are inserted with the album's `albumId` and a null
   `folderId`; URIs the album already holds are skipped
4. Nothing is written back to the album row — ownership is the child's foreign key, and covers are
   derived

### Manual Set
`Set Home` / `Set Lock` / `Set Both` from the wallpaper menu calls
`UpdateCoordinator.applyWallpaper()`: the chosen row is applied immediately, `currentWallpaperId` is
written for the applied screens (`Both` writes both) and `lastUpdated` advances, so the Home preview
and `CURRENT` follow the change. With mirroring on, every target is applied and recorded as `Both`,
`Set Home` included, so Home and Lock never diverge. The schedule re-arms from the new timestamp. A
failed manual apply is returned as `Failed` but not recorded in `failure`, so the schedule keeps its
existing deadline and does not retry that target; the next due cycle proceeds normally.

### Wallpaper Auto-Cycle (Summary)
1. `TickScheduler` keeps exactly one `AlarmManager` tick armed at `config.nextUpdate` (`lastUpdated + interval`)
2. `TickReceiver` handles the tick plus `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` and `MY_PACKAGE_REPLACED`, and forwards all of them to `UpdateCoordinator.applyIfDue()`
3. `applyIfDue()` calls `WallpaperCycler.cycleIfDue()`, which applies the wallpaper if due (or retries the recorded failed target), and re-arms the tick from the new `lastUpdated` — or, when an apply failed, a bounded 1–5 minute retry
4. `FallbackWorker` re-evaluates the same rule every 15 minutes in case the alarm was dropped; `SyncWorker` delegates to `UpdateCoordinator.refresh()` so albums are synced and the config reconciled off the timed path; `WallmaticActivity.onStart` applies the cycle if due on every return to the foreground

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

- **Local Modules**: `:core:common`, `:core:ui`, `:core:devtools`, `:core:datastore`, `:core:room`
- **AndroidX**: Compose BOM + Material3, Navigation Compose, Room, Proto + Preferences DataStore, DocumentFile, WorkManager
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
