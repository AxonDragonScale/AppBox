> Context: [Main](../../context.md) > [Wallmatic](./context.md) > Background Services

# Wallmatic — Background Services

This document covers the background wallpaper update system: scheduling, workers, broadcast receivers, and quick settings tiles. For module overview, database schema, and config, see [Wallmatic context](./context.md).

## Component Architecture

```
WallpaperWorker (OneTimeWork, at calculated next-update time)
    ↓ calls
SyncManager.syncAlbums()
    ↓ then
WallpaperUpdater.updateWallpaper()
    ↓ uses
WallpaperManager.setStream() (system API)
    ↓ then
WallpaperWorkScheduler.scheduleNextUpdate() → enqueues next WallpaperWorker

WatchdogWorker (PeriodicWork, every 15 min)
    ↓ queries WorkManager for WALLPAPER_WORK_NAME state
    ↓ if not ENQUEUED or RUNNING:
WallpaperWorkScheduler.scheduleNextUpdate() → revives the chain
```

## Why WorkManager?

The previous stack (`AlarmManager.setExact` → `BroadcastReceiver` → `ForegroundService`) was unreliable on OEM-skinned Android (Xiaomi, Samsung, Oppo, etc.) due to aggressive battery optimisation. WorkManager is whitelisted by OEMs in a way that arbitrary alarms and custom services are not. It also persists pending work across reboots natively.

## WallpaperWorker (`background/WallpaperWorker.kt`)

`CoroutineWorker` that coordinates each scheduled update cycle:

1. `syncManager.syncAlbums()` — sync filesystem before applying
2. `wallpaperUpdater.updateWallpaper()` — select and apply wallpaper
3. `scheduler.scheduleNextUpdate()` — enqueue the next run
4. Returns `Result.success()` on completion, `Result.retry()` on failure (triggers WorkManager backoff)

Scheduling is the worker's responsibility, not `WallpaperUpdater`'s — so tile-triggered updates don't accidentally reschedule.

## WallpaperUpdater (`core/WallpaperUpdater.kt`)

Core logic for selecting and applying wallpapers.

- No-arg `updateWallpaper()`: reads config, checks if interval has passed, determines target (`Home` / `Lock` / `Both`), delegates to the `TargetScreen` overload
- `updateWallpaper(target)`: loads album(s), picks random wallpaper(s), calls `WallpaperManager.setStream()`, updates `currentWallpaperId` and `lastUpdated` in config
- When `mirrorHomeConfigForLock = true`: single pick from home album applied to both screens
- Explicit `TargetScreen` overload bypasses the interval check — used by tile services for immediate updates

## WallpaperWorkScheduler (`background/WallpaperWorkScheduler.kt`)

Wraps WorkManager enqueuing. Uses `ExistingWorkPolicy.REPLACE` so a re-schedule always supersedes the pending one.

| Method | Description |
|---|---|
| `scheduleNextUpdate()` | Reads config, computes `delay = config.nextUpdate - now` (clamped to 0), enqueues `WallpaperWorker` |
| `cancelScheduledUpdates()` | Cancels the pending update work |
| `scheduleWatchdog()` | Enqueues `WatchdogWorker` as periodic work; uses `ExistingPeriodicWorkPolicy.KEEP` so repeated calls are no-ops |

Work names: `"wallpaper_update"` (`WALLPAPER_WORK_NAME`), `"wallpaper_watchdog"` (`WATCHDOG_WORK_NAME`).

## WatchdogWorker (`background/WatchdogWorker.kt`)

Periodic `CoroutineWorker` (15-minute interval) that acts as a dead-man's switch for the one-time update chain.

- Queries WorkManager for the state of `WALLPAPER_WORK_NAME`
- If no work is `ENQUEUED` or `RUNNING`, calls `scheduler.scheduleNextUpdate()` to revive the chain
- Uses `getWorkInfosForUniqueWorkFlow().first()` — no extra dependencies required
- Returns `Result.success()` unconditionally (watching is always a valid outcome)

The chain can die silently from OEM task killers or force-stops that aren't covered by `WallmaticReceiver`. The watchdog bounds the worst-case missed-update window to 15 minutes.

`scheduleWatchdog()` is called from `WallmaticActivity.onCreate()` and from `WallmaticReceiver` on every handled action. The `KEEP` policy means all but the first call are free no-ops.

## WallmaticReceiver (`background/WallmaticReceiver.kt`)

Handles system events that can invalidate the scheduled update time or wipe WorkManager state entirely. All actions call `scheduleNextUpdate()` + `scheduleWatchdog()` (the `KEEP` policy makes the watchdog call a no-op when it's already running).

| Action | Reason |
|---|---|
| `BOOT_COMPLETED` | WorkManager persists work but the calculated delay may be stale after a reboot |
| `TIME_SET` | User-changed clock shifts the next update time |
| `TIMEZONE_CHANGED` | Same as above |
| `MY_PACKAGE_REPLACED` | An app update wipes all pending WorkManager state — both the update chain and the watchdog must be re-established from scratch |

## SyncManager (`core/SyncManager.kt`)

Synchronizes album contents with the filesystem before each wallpaper update.

- Scans folder URIs for added/removed image files and updates Room accordingly
- `syncConfig()`: resets `isInit = false` if the configured album is gone/empty; triggers an immediate `updateWallpaper()` if `currentWallpaperId` is stale
- Called by `WallpaperWorker` before each update, and by `WallmaticActivity` on app start

SAF integration: folder URIs are persisted with `takePersistableUriPermission()`; `DocumentFile.fromTreeUri()` is used to list files.

## Quick Settings Tiles

Three `TileService`s for immediate manual wallpaper changes from the notification shade:

| Service | Target | Icon |
|---|---|---|
| `WallmaticTileService` | Both screens | `ic_replace_image` |
| `HomeTileService` | Home only | `ic_home_replace_image` |
| `LockTileService` | Lock only | `ic_lock_replace_image` |

Tile taps call `wallpaperUpdater.updateWallpaper(targetScreen)` directly — interval check bypassed, scheduled chain unaffected.

## Data Flows

### Auto-Cycle

```
1. App start or previous update completes
       ↓
2. WallpaperWorkScheduler.scheduleNextUpdate()
       ↓ delay elapses
3. WallpaperWorker.doWork()
       ↓
4. SyncManager.syncAlbums()
       ↓
5. WallpaperUpdater.updateWallpaper()
       ↓
6. Random wallpaper selected → WallpaperManager.setStream()
       ↓
7. Config updated: currentWallpaperId, lastUpdated
       ↓
8. WallpaperWorkScheduler.scheduleNextUpdate() → chain continues
```

### Quick Settings Tile

```
1. User taps tile
       ↓
2. WallpaperUpdater.updateWallpaper(targetScreen)  ← interval check bypassed
       ↓
3. Random wallpaper selected → WallpaperManager.setStream()
       ↓
4. Config updated: currentWallpaperId, lastUpdated
   (scheduled chain unchanged)
```

### Device Reboot

```
1. Device boots
       ↓
2. WallmaticReceiver receives BOOT_COMPLETED
       ↓
3. WallpaperWorkScheduler.scheduleNextUpdate()
       ↓
4. Delay = lastUpdated + updateInterval - now (0 if past due)
       ↓
5. WallpaperWorker enqueued
```

## Permissions

```xml
<uses-permission android:name="android.permission.SET_WALLPAPER" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## Related Context

- [Wallmatic Context](./context.md) — Module overview, database schema, config schema
- [Commons Module](../../core/commons/context.md) — Shared utilities
- [Main Context](../../context.md) — Project overview, tech stack