> Context: [Main](../../context.md) > [Wallmatic](./context.md) > Background Services

# Wallmatic — Background Services

This document covers the wallpaper update system: what triggers a cycle, how the timer survives Doze and reboots, and the workers around it. For module overview, database schema and config, see [Wallmatic context](./context.md).

## The rule

```
nextUpdate = lastUpdated + updateInterval
```

A cycle is due once `nextUpdate` has passed. Every trigger runs the same rule — apply if due, then leave exactly one wake-up armed. The schedule is derived from `lastUpdated` in the DataStore, never encoded in a chain of enqueued work, so a lost trigger costs at most one cycle and any later trigger repairs it.

`UpdateCoordinator.reconcile()` is the only implementation, guarded by a `Mutex` so an alarm, an app start and a fallback run cannot apply twice.

| Outcome of the attempt | Next wake-up |
|---|---|
| `Updated` | `nextUpdate` computed from the new `lastUpdated`, plus an enqueued `SyncWorker` |
| `NotDue` | `nextUpdate` |
| `Failed` | `now + updateInterval`, clamped to 1–5 minutes, plus an enqueued `SyncWorker` |
| `Skipped` | the same bounded retry |
| config missing, `isInit` false, or auto-cycle off everywhere | alarm cancelled |

`Failed` and `Skipped` do not advance `lastUpdated`, so a normal arm would be due immediately and loop; the bounded retry is what prevents that. `SyncWorker` is enqueued on those outcomes so a wallpaper deleted from disk is pruned before the retry picks again.

## TickScheduler — the timer

One `AlarmManager` alarm: `RTC_WAKEUP` with `setAndAllowWhileIdle`, one explicit `PendingIntent` at a fixed request code. Setting an alarm replaces the previous one, so re-arming is idempotent and a duplicate tick is impossible.

Why not the alternatives:

- **`setExactAndAllowWhileIdle`** — precise, but needs `SCHEDULE_EXACT_ALARM`, which Android 14+ denies by default to apps targeting API 33+; the user would have to grant it in settings.
- **`PeriodicWorkRequest`** — 15-minute floor plus up to 5 minutes of flex, and the job is deferrable, so a 15-minute interval would drift to 20–30.
- **`OneTimeWorkRequest` with `setInitialDelay`** — the same JobScheduler deferral as any other job: fine for housekeeping, useless as a clock.

`setAndAllowWhileIdle` needs no permission and is delivered during Doze, throttled to roughly the interval being scheduled. In active use alarms are batched to about a minute, so a 15-minute interval lands within about a minute of due.

## TickReceiver — the triggers

One receiver for everything that starts or invalidates a tick.

| Action | Why |
|---|---|
| `ACTION_TICK` | the alarm itself |
| `BOOT_COMPLETED` | alarms do not survive a reboot |
| `TIME_SET`, `TIMEZONE_CHANGED` | an `RTC` trigger is wall-clock, so the deadline must be re-armed after a clock change |
| `MY_PACKAGE_REPLACED` | an app update clears all alarms |

`onReceive` arms a provisional tick at `now + MAX_RETRY_DELAY_MILLIS` *before* calling `goAsync()`. The alarm that fired has already been consumed, so without this a process death during the update would leave nothing armed at all. `reconcile()` overwrites the provisional with the exact deadline.

## FallbackWorker — the durability net

A 15-minute periodic `CoroutineWorker` whose whole body is `reconcile()`. It is not the timer and its imprecision does not matter: in the normal case it finds the cycle not due and does a single DataStore read.

It exists because an alarm lives in `system_server` and can be cleared without the app ever learning about it — OEM battery managers, force-stops, an update whose broadcast was missed. WorkManager persists its own state in a database and reschedules after a reboot, so this is the layer that cannot silently vanish. Both layers evaluate the same idempotent rule, so running them together is harmless.

## SyncWorker

Plain one-shot `CoroutineWorker` (unique work name, `KEEP`) that runs `SyncManager.syncAlbums()`. Enqueued by `UpdateCoordinator` after an applied or failed cycle, and called directly by `WallmaticActivity.onCreate`.

It is deliberately off the timed path: `syncAlbums()` scans SAF folders and can take seconds, while a tick runs inside the ~10-second window a doze-exempt alarm grants a receiver. Applying a wallpaper only needs Room, so the tick stays IO-bound.

## WallpaperUpdater (`core/WallpaperUpdater.kt`)

Core logic for selecting and applying wallpapers.

- No-arg `updateWallpaper()`: reads config, checks if the interval has passed, determines target (`Home` / `Lock` / `Both`), delegates to the `TargetScreen` overload. Returns `NotDue` if the interval has not elapsed, `Skipped` when nothing was applied for a non-failure reason (unusable config, empty album whitelist), otherwise whatever the delegated update returned
- `updateWallpaper(target)`: loads album(s), picks random wallpaper(s), calls `WallpaperManager.setStream()`, then persists config for the screens that actually applied — `currentWallpaperId` per applied screen, `lastUpdated` as soon as at least one screen applied. Returns `Updated`, `Skipped(reason)` (config null / not initialised / album missing / album has no whitelisted wallpaper) or `Failed(cause)`
- An album whose whitelist is empty (every wallpaper blacklisted) is not a failure: `getRandomWallpaperOrNull()` returns `null`, that screen is skipped with a warning naming the album id, and the other screen is still applied. If no screen applied and nothing threw, the result is `Skipped(reason)`; a failure cause always wins over a skip reason, and `Failed` stays reserved for an apply that actually failed
- A failed apply is never persisted as a success: if nothing applied, no config is written at all, `lastUpdated` stays unadvanced and `Failed` is returned. If only some screens applied, the successes are persisted, `lastUpdated` advances and `Failed` still carries the first failure
- When `mirrorHomeConfigForLock = true`: single pick from home album applied to both screens
- The explicit `TargetScreen` overload bypasses the interval check — used by `UpdateCoordinator.applyNow()` for tiles and explicit album changes

## Quick Settings Tiles

Three `TileService`s for immediate manual wallpaper changes from the notification shade:

| Service | Target | Icon |
|---|---|---|
| `WallmaticTileService` | Both screens | `ic_replace_image` |
| `HomeTileService` | Home only | `ic_home_replace_image` |
| `LockTileService` | Lock only | `ic_lock_replace_image` |

Tile taps call `UpdateCoordinator.applyNow(target)`: the interval check is bypassed, config is persisted for the applied screens, and the tick is re-armed from the new `lastUpdated` — so a manual change also pushes the next automatic one out by a full interval.

## Data Flows

### Scheduled cycle

```
1. Alarm fires at lastUpdated + updateInterval
       ↓
2. TickReceiver arms a provisional tick, then goAsync()
       ↓
3. UpdateCoordinator.reconcile()          (mutex)
       ↓
4. WallpaperUpdater.updateWallpaper()     ← interval check
       ↓
5. setStream() → persist currentWallpaperId + lastUpdated
       ↓
6. arm the next tick at the new nextUpdate, enqueue SyncWorker
```

If step 5 applies nothing, step 6 becomes the bounded retry instead, and `SyncWorker` is enqueued so the next attempt sees a pruned album.

### App start

```
1. WallmaticActivity.onCreate() → SyncManager.syncAlbums()
       ↓
2. WallmaticActivity.onStart()  → UpdateCoordinator.reconcile()
```

`onStart` fires on first launch and on every return to the foreground, including after the system picker closes. `updateWallpaper()` honours the interval, so the normal case is a cheap `NotDue` no-op; an overdue cycle is applied instead of waiting for a deferred alarm. `syncAlbums()` stays in `onCreate` because syncing in `onStart` races with the picker writing the same albums.

### Reboot / app update

```
1. BOOT_COMPLETED or MY_PACKAGE_REPLACED
       ↓
2. TickReceiver → reconcile() → tick re-armed, fallback re-ensured
```

A clock change needs no immediate update: the next run re-evaluates `lastUpdated + updateInterval` itself.

## Permissions

```xml
<uses-permission android:name="android.permission.SET_WALLPAPER" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

No `SCHEDULE_EXACT_ALARM`: the tick uses an inexact doze-exempt alarm on purpose.

## Related Context

- [Wallmatic Context](./context.md) — Module overview, database schema, config schema
- [Commons Module](../../core/commons/context.md) — Shared utilities
- [Main Context](../../context.md) — Project overview, tech stack
