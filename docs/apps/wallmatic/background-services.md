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
WallpaperWorkScheduler.appendNextUpdate() → appends the next WallpaperWorker
                                            as a dependent of the current run
                                            (a cycle that applied nothing calls
                                             appendRetry() instead)

WatchdogWorker (PeriodicWork, every 15 min)
    ↓ queries WorkManager for WALLPAPER_WORK_NAME state
    ↓ if nothing is ENQUEUED, RUNNING or BLOCKED:
WallpaperWorkScheduler.ensureScheduled() → revives the chain
```

## Why WorkManager?

The previous stack (`AlarmManager.setExact` → `BroadcastReceiver` → `ForegroundService`) was unreliable on OEM-skinned Android (Xiaomi, Samsung, Oppo, etc.) due to aggressive battery optimisation. WorkManager is whitelisted by OEMs in a way that arbitrary alarms and custom services are not. It also persists pending work across reboots natively.

## WallpaperWorker (`background/WallpaperWorker.kt`)

`CoroutineWorker` that coordinates each scheduled update cycle:

1. `syncManager.syncAlbums()` — sync filesystem before applying (a failure is logged inside `SyncManager`, see below)
2. `wallpaperUpdater.updateWallpaper()` — select and apply wallpaper
3. Appends exactly one successor to the chain: `scheduler.appendNextUpdate()` for `Updated` / `NotDue`, `scheduler.appendRetry()` for anything that applied nothing (`Failed` / `Skipped`)
4. Always returns `Result.success()` — the successor owns recovery, so `Result.retry()` is never used

Invariant: **every non-cancelled run appends exactly one successor**. `Result.retry()` is never returned, because a retrying work instance would compete with that successor under the same unique work name.

Appending is what makes scheduling from inside `doWork()` safe. The successor is enqueued with `ExistingWorkPolicy.APPEND_OR_REPLACE`, so it becomes a *dependent* of the running work (`BLOCKED` until that run finishes) instead of replacing it. `ExistingWorkPolicy.REPLACE` must never be used on a worker-side path: it cancels the work that is executing the call, and the chain would end right there.

A `Failed` update, a `Skipped` update, or an exception thrown anywhere in the cycle logs on its own level and calls `scheduler.appendRetry()` instead of `appendNextUpdate()`. Neither result advances `lastUpdated`, so a normal append would be due immediately and would loop; the retry gives the chain a bounded 1–5 minute delay instead. That is also why a skip is not left on the normal schedule: "nothing to apply" is a state that can persist (every wallpaper blacklisted), and retrying it immediately would keep the chain spinning through a full album sync each pass.

Cancellation is rethrown (`CancellationException`): a cancelled run neither schedules a successor nor reports an outcome — WorkManager/the watchdog revives the chain.

`syncManager.syncAlbums()` is housekeeping and guards itself: it logs its own failure at error level and returns, so a sync problem never blocks the user-visible wallpaper change or app start.

Scheduling is the worker's responsibility, not `WallpaperUpdater`'s — so tile-triggered updates don't accidentally reschedule.

## WallpaperUpdater (`core/WallpaperUpdater.kt`)

Core logic for selecting and applying wallpapers.

- No-arg `updateWallpaper()`: reads config, checks if interval has passed, determines target (`Home` / `Lock` / `Both`), delegates to the `TargetScreen` overload. Returns `NotDue` if the interval has not elapsed, `Skipped` when nothing was applied for a non-failure reason (unusable config, empty album whitelist), otherwise whatever the delegated update returned
- `updateWallpaper(target)`: loads album(s), picks random wallpaper(s), calls `WallpaperManager.setStream()`, then persists config for the screens that actually applied — `currentWallpaperId` per applied screen, `lastUpdated` as soon as at least one screen applied. Returns `Updated`, `Skipped(reason)` (config null / not initialised / album missing / album has no whitelisted wallpaper) or `Failed(cause)`
- An album whose whitelist is empty (every wallpaper blacklisted) is not a failure: `getRandomWallpaperOrNull()` returns `null`, that screen is skipped with a warning naming the album id, and the other screen is still applied. If no screen applied and nothing threw, the result is `Skipped(reason)`; a failure cause always wins over a skip reason, and `Failed` stays reserved for an apply that actually failed
- A failed apply is never persisted as a success: if nothing applied, no config is written at all, `lastUpdated` stays unadvanced and `Failed` is returned. If only some screens applied, the successes are persisted, `lastUpdated` advances and `Failed` still carries the first failure
- When `mirrorHomeConfigForLock = true`: single pick from home album applied to both screens
- Explicit `TargetScreen` overload bypasses the interval check — used by tile services for immediate updates

## WallpaperWorkScheduler (`background/WallpaperWorkScheduler.kt`)

Wraps WorkManager enqueuing for the one-time update chain. Every entry point shares one private
helper, so the config read, the guards and the work name live in one place; only the
`ExistingWorkPolicy` and the delay differ per caller.

| Method | Policy | Delay | When used |
|---|---|---|---|
| `appendNextUpdate()` | `APPEND_OR_REPLACE` | `config.nextUpdate - now`, clamped at 0 | `WallpaperWorker` after `Updated` / `NotDue` — extend the chain without cancelling the run |
| `appendRetry()` | `APPEND_OR_REPLACE` | `config.updateInterval` clamped to `[1 min, RETRY_DELAY_MILLIS]` (5 min) | `WallpaperWorker` after a cycle that applied nothing (`Failed` / `Skipped`) or threw |
| `ensureScheduled()` | `KEEP` | `config.nextUpdate - now`, clamped at 0 | `WallmaticActivity.onCreate()`, `WallmaticReceiver`, `WatchdogWorker` — idempotent registration, never disturbs a pending or in-flight run |
| `reschedule()` | `REPLACE` | `config.nextUpdate - now`, clamped at 0 | `HomeVM` when the schedule inputs changed (album selected, mirroring toggled, auto-cycle toggled, interval updated) |
| `cancelScheduledUpdates()` | — | — | Cancels the pending update work (auto-cycle turned off everywhere) |
| `scheduleWatchdog()` | `KEEP` (periodic) | 15 min period | Enqueues `WatchdogWorker`; repeated calls are no-ops |

`REPLACE` is only safe outside the worker: it cancels whatever is pending *or currently running*
under the work name. The worker therefore only ever appends.

With the append policies the delay is derived from the cycle that just finished: the worker appends
after writing `lastUpdated`, so the successor is scheduled one interval after the update it follows.

### Why a one-time chain instead of one periodic worker

The tempting simplification — a single `PeriodicWorkRequest` replacing both the chain and the
watchdog — cannot hold this app's intervals. WorkManager's floor for periodic work is 15 minutes
(`MIN_PERIODIC_INTERVAL_MILLIS`), and each run may happen anywhere in the last 5 minutes of its
period (`MIN_PERIODIC_FLEX_MILLIS`), so consecutive runs land 10–15 minutes apart at unpredictable
moments. `WallpaperUpdater.updateWallpaper()` only applies when `config.nextUpdate < now`
(`lastUpdated + interval`), so a run that lands just *before* the due moment is skipped and the
update waits for the following period: a user-selected 15-minute interval would end up changing the
wallpaper every ~20–30 minutes. The one-time chain with an explicit delay keeps 15/30/45/60 minute
intervals exact, and is also what keeps the 1-minute dev fast cycle
(`AppPrefsRepository.setFastAutoCycle`) usable.

Work names: `"wallpaper_update"` (`WALLPAPER_WORK_NAME`), `"wallpaper_watchdog"` (`WATCHDOG_WORK_NAME`). Retry delay bound: `RETRY_DELAY_MILLIS` (5 minutes). The retry delay is floored at 1 minute so a zero/invalid `updateInterval` cannot produce a zero-delay loop.

## WatchdogWorker (`background/WatchdogWorker.kt`)

Periodic `CoroutineWorker` (15-minute interval) that acts as a dead-man's switch for the one-time update chain.

- Queries WorkManager for the state of `WALLPAPER_WORK_NAME`
- If no work is `ENQUEUED`, `RUNNING` or `BLOCKED`, calls `scheduler.ensureScheduled()` to revive the chain
- `BLOCKED` counts as alive: the successor a running worker appends is that run's dependent, and dependents stay `BLOCKED` until the run finishes. Treating only `ENQUEUED`/`RUNNING` as alive would make the watchdog declare the chain dead during every run and replace it — cancelling the run it exists to protect
- `ensureScheduled()` uses `KEEP`, so a live chain is never disturbed; the watchdog only ever creates work when there is none
- Uses `getWorkInfosForUniqueWorkFlow().first()` — no extra dependencies required
- Returns `Result.success()` unconditionally (watching is always a valid outcome)

The chain can die silently from OEM task killers or force-stops that aren't covered by `WallmaticReceiver`. The watchdog bounds the worst-case missed-update window to 15 minutes.

`scheduleWatchdog()` is called from `WallmaticActivity.onCreate()` and from `WallmaticReceiver` on every handled action. The `KEEP` policy means all but the first call are free no-ops.

## WallmaticReceiver (`background/WallmaticReceiver.kt`)

Handles system events that can wipe the scheduled update chain. All actions call `ensureScheduled()` + `scheduleWatchdog()`; both use `KEEP`, so a chain that is already pending or running is left untouched (and the watchdog call is a no-op when it is already scheduled).

The schedule itself is derived from `lastUpdated` in the config, so the receiver's job is only "make sure the chain exists". It is idempotent, and a clock change needs no immediate reschedule because the next run re-evaluates the due check (`lastUpdated + updateInterval`) by itself.

| Action | Reason |
|---|---|
| `BOOT_COMPLETED` | WorkManager persists work, but a reboot is the moment to make sure the chain and watchdog exist |
| `TIME_SET` | The due check is re-evaluated by the next run, so only the chain's existence matters here |
| `TIMEZONE_CHANGED` | Same as above |
| `MY_PACKAGE_REPLACED` | An app update wipes all pending WorkManager state — both the update chain and the watchdog must be re-established from scratch |

## SyncManager (`core/SyncManager.kt`)

Synchronizes album contents with the filesystem before each wallpaper update.

- Scans folder URIs for added/removed image files and updates Room accordingly
- `syncConfig()`: resets `isInit = false` if the configured album is gone/empty; triggers an immediate `updateWallpaper()` if `currentWallpaperId` is stale
- Called by `WallpaperWorker` before each update, by `WallmaticActivity` on app start, and by dev tools
- A failure anywhere in `syncAlbums()` is logged at error level and swallowed (`CancellationException` is rethrown): syncing is housekeeping and must not interrupt its callers

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
1. App start, receiver, or previous update completing
       ↓
2. WallpaperWorkScheduler.ensureScheduled()   (app start / receiver, KEEP: no-op
       ↓                                       when a chain already exists) or
       ↓                                       appendNextUpdate() (from the worker)
       ↓ delay elapses
3. WallpaperWorker.doWork()
       ↓
4. SyncManager.syncAlbums()   ← a failure here is logged, the cycle continues
       ↓
5. WallpaperUpdater.updateWallpaper()
       ↓
6. Random wallpaper selected → WallpaperManager.setStream()
       ↓
7. Config updated for applied screens: currentWallpaperId, lastUpdated
       ↓
8. WallpaperWorkScheduler.appendNextUpdate() → chain continues

If step 6 fails, step 7 persists nothing and step 8 becomes
WallpaperWorkScheduler.appendRetry() — the chain continues after a bounded
1–5 minute retry delay instead of looping on a due-now schedule or waiting on
WorkManager's multi-hour backoff.

If an album's whitelist is empty (every wallpaper blacklisted), that screen is
skipped with a warning naming the album id: steps 6 and 7 do not happen for it
and the other screen is still applied. When no screen applied at all, step 8 is
also a retry rather than the normal append, for the same reason as a failure.
```

### Quick Settings Tile

```
1. User taps tile
       ↓
2. WallpaperUpdater.updateWallpaper(targetScreen)  ← interval check bypassed
       ↓
3. Random wallpaper selected → WallpaperManager.setStream()
       ↓
4. Config updated for applied screens: currentWallpaperId, lastUpdated
   (nothing persisted when the apply fails; scheduled chain unchanged)
```

### Device Reboot

```
1. Device boots
       ↓
2. WallmaticReceiver receives BOOT_COMPLETED
       ↓
3. WallpaperWorkScheduler.ensureScheduled()   (KEEP: no-op when a chain is already
       ↓                                       pending or running)
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