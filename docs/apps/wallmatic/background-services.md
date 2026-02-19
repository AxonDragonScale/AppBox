> Context: [Main](../../context.md) > [Wallmatic](./context.md) > Background Services

# Wallmatic — Background Services

This document covers Wallmatic's background wallpaper update system, including scheduling, broadcast receivers, foreground service, quick settings tiles, and filesystem sync. For general Wallmatic context, see [Wallmatic context](./context.md).

## Overview

Wallmatic's background system ensures wallpapers are automatically changed on schedule, even after device reboots or timezone changes. The system uses `AlarmManager` for precise scheduling, a `ForegroundService` for the actual wallpaper update, `BroadcastReceiver`s for system event handling, and Quick Settings tiles for manual triggers.

## Component Architecture

```
AlarmManager (exact alarm)
    ↓ triggers
WallmaticReceiver (BroadcastReceiver)
    ↓ starts
WallmaticService (ForegroundService)
    ↓ calls
WallpaperUpdater (core logic)
    ↓ uses
WallpaperManager.setStream() (system API)
    ↓ then
WallmaticScheduler → schedules next alarm
```

## WallpaperUpdater (`core/WallpaperUpdater.kt`)

The central piece — contains all logic for selecting and applying wallpapers.

### Responsibilities

- Check if the update interval has elapsed since last update
- Select a random wallpaper from the whitelisted pool in the configured album
- Apply the wallpaper to the correct target screen(s)
- Update the config with the new wallpaper ID and timestamp
- Schedule the next update after completion

### Key Behaviors

- Supports separate home/lock screen configurations
- **Mirroring option**: When `mirrorHomeConfigForLock` is true, the same wallpaper is applied to both screens
- Selects a **random** wallpaper from the whitelisted pool (non-blacklisted wallpapers in the album)
- Uses `WallpaperManager.setStream()` with appropriate flags:
  - `FLAG_SYSTEM` for home screen
  - `FLAG_LOCK` for lock screen
- Skips update if the configured interval hasn't passed (unless forced)

### Target Screen Handling

```kotlin
enum class TargetScreen {
    Home,   // Home screen only
    Lock,   // Lock screen only
    Both    // Both screens
}
```

When `mirrorHomeConfigForLock = true`:
- A single wallpaper is selected from the home config's album
- Applied to both home and lock screens simultaneously

When `mirrorHomeConfigForLock = false`:
- Each screen has its own `WallpaperConfig` (album, interval, current wallpaper)
- Updates are independent — each screen can have a different album and schedule

## WallmaticScheduler (`background/WallmaticScheduler.kt`)

Manages exact alarms for auto-cycle updates.

### How It Works

- Uses `AlarmManager.setExactAndAllowWhileIdle()` for precise timing
- Calculates the next trigger time based on:
  - `lastUpdated` timestamp from config
  - `updateInterval` from config
- Creates a `PendingIntent` targeting `WallmaticReceiver`
- Called after every successful wallpaper update to schedule the next one

### Permissions Required

```xml
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

## WallmaticReceiver (`background/WallmaticReceiver.kt`)

A `BroadcastReceiver` that handles system events and alarm triggers.

### Handled Actions

| Action | When | Response |
|--------|------|----------|
| Alarm trigger | Scheduled update time reached | Starts `WallmaticService` to perform update |
| `BOOT_COMPLETED` | Device reboots | Reschedules alarms (alarms don't survive reboot) |
| `TIME_SET` / `TIMEZONE_CHANGED` | User changes time/timezone | Reschedules alarms to maintain correct intervals |

### Boot Persistence

Android clears all alarms on reboot. `WallmaticReceiver` listens for `BOOT_COMPLETED` and re-registers the exact alarm based on the persisted config (last update time + interval).

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## WallmaticService (`background/WallmaticService.kt`)

A `ForegroundService` that performs the actual wallpaper update.

### Why a Foreground Service?

- Wallpaper updates involve reading potentially large image files from storage
- Background execution limits on Android 12+ would kill a plain background task
- A foreground service with a notification ensures the update completes reliably

### Lifecycle

1. Started by `WallmaticReceiver` (or Quick Settings tiles)
2. Shows a foreground notification
3. Calls `WallpaperUpdater.updateWallpaper()` with the appropriate `TargetScreen`
4. After completion, stops itself
5. `WallpaperUpdater` internally schedules the next alarm via `WallmaticScheduler`

## Quick Settings Tiles

Three tile services for manual wallpaper changes directly from the notification shade:

### WallmaticTileService (`background/WallmaticTileService.kt`)

- Changes **both** home and lock screen wallpapers
- Icon: `ic_replace_image`

### HomeTileService (`background/HomeTileService.kt`)

- Changes **home screen** wallpaper only
- Icon: `ic_home_replace_image`

### LockTileService (`background/LockTileService.kt`)

- Changes **lock screen** wallpaper only
- Icon: `ic_lock_replace_image`

### Tile Behavior

1. User taps tile → `TileService.onClick()` is called
2. Service calls `WallpaperUpdater.updateWallpaper(targetScreen)`
3. Wallpaper is changed **immediately** (ignores interval check)
4. The next **scheduled** update remains unchanged — the tile is a one-off manual action

## SyncManager (`core/SyncManager.kt`)

Synchronizes album contents with the device filesystem.

### Purpose

Folders added to albums reference filesystem directories via SAF (Storage Access Framework) URIs. Files can be added or removed externally (e.g., user downloads new wallpapers or deletes old ones). SyncManager detects these changes.

### Behavior

- **Detects added wallpapers**: Scans folder URIs for new image files not yet in the database
- **Detects removed wallpapers**: Identifies database entries whose files no longer exist on disk
- **Updates database**: Inserts new wallpaper entries, removes stale ones
- **Called on app start**: `WallmaticActivity` triggers a sync when the app is opened

### SAF Integration

Uses `DocumentFile` API to traverse folder contents:
- Folder URIs are persisted with `takePersistableUriPermission()` to survive app restarts
- `DocumentFile.fromTreeUri()` is used to list files within selected folders
- Only image MIME types are considered as wallpapers

## Data Flow: Wallpaper Auto-Cycle

```
1. App start or previous update completion
       ↓
2. WallmaticScheduler.schedule()
       ↓ sets exact alarm
3. AlarmManager triggers at scheduled time
       ↓
4. WallmaticReceiver.onReceive()
       ↓ starts service
5. WallmaticService (foreground)
       ↓ calls
6. WallpaperUpdater.updateWallpaper()
       ↓
7. Read config from Proto DataStore
       ↓
8. Check if interval has passed (skip if not)
       ↓
9. Load album → get whitelisted wallpapers
       ↓
10. Select random wallpaper from pool
       ↓
11. WallpaperManager.setStream(inputStream, flags)
       ↓
12. Update config: currentWallpaperId, lastUpdated
       ↓
13. WallmaticScheduler.schedule() → next alarm
       ↓
14. Service stops itself
```

## Data Flow: Quick Settings Tile

```
1. User taps tile in notification shade
       ↓
2. TileService.onClick()
       ↓
3. WallpaperUpdater.updateWallpaper(targetScreen)
       ↓ (interval check bypassed)
4. Load album → select random wallpaper
       ↓
5. WallpaperManager.setStream()
       ↓
6. Update config (currentWallpaperId, lastUpdated)
       ↓
7. Scheduled alarm remains unchanged
```

## Data Flow: Device Reboot

```
1. Device boots up
       ↓
2. WallmaticReceiver receives BOOT_COMPLETED
       ↓
3. Read persisted config from Proto DataStore
       ↓
4. Calculate next update time (lastUpdated + interval)
       ↓
5. WallmaticScheduler.schedule() → re-registers alarm
       ↓
6. If next update time is in the past → trigger immediately
```

## Configuration Reference

The background services read from Proto DataStore config (see [Wallmatic context](./context.md) for full schema):

```protobuf
message WallpaperConfig {
    int32 albumId = 1;           // Which album to draw wallpapers from
    bool autoCycleEnabled = 2;   // Whether auto-cycle is active
    int32 currentWallpaperId = 3; // Currently displayed wallpaper
    int64 updateInterval = 4;    // Millis between updates
    int64 lastUpdated = 5;       // Epoch millis of last update
}
```

Key fields for background services:
- `autoCycleEnabled`: If false, no alarms are scheduled
- `updateInterval`: Determines when the next alarm fires
- `lastUpdated`: Used to calculate the next trigger time
- `albumId`: Determines which album's wallpapers to choose from

## Permissions Summary

```xml
<uses-permission android:name="android.permission.SET_WALLPAPER" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

## Related Context

- [Wallmatic Context](./context.md) — Module overview, database schema, UI screens, Proto DataStore config
- [Commons Module](../../core/commons/context.md) — Shared utilities
- [Main Context](../../context.md) — Project overview, tech stack