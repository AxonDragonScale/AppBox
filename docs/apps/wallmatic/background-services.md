---
title: "Wallmatic — background services"
status: current
summary: "The nextUpdate = lastUpdated + interval rule, how Doze, reboots, alarms and workers keep exactly one wake-up armed, and how a saved crop is re-applied."
read_when: "Debugging missed updates, Doze or reboot behaviour, or alarm and worker wiring."
---

> Context: [Main](../../context.md) > [Wallmatic](./context.md) > Background Services

# Wallmatic — Background Services

This document covers the wallpaper update system: what triggers a cycle, how the timer survives Doze and reboots, and the workers around it. For module overview, database schema and config, see [Wallmatic context](./context.md).

## The rule

```
nextUpdate = lastUpdated + interval
```

A cycle is due once `nextUpdate` has passed. Every trigger runs the same rule — apply if due, then leave exactly one wake-up armed. The schedule is derived from `lastUpdated` in the DataStore, never encoded in a chain of enqueued work, so a lost trigger costs at most one cycle and any later trigger repairs it.

`UpdateCoordinator.applyIfDue()` is the only scheduled entry point, guarded by a `Mutex` so an alarm,
an app start and a fallback run cannot apply twice.

| Outcome of the attempt | Next wake-up |
|---|---|
| `Updated` | `nextUpdate` computed from the new `lastUpdated`, plus an enqueued `SyncWorker` |
| `NotDue` | `nextUpdate` |
| `Failed` | `now + interval`, clamped to 1–5 minutes, plus an enqueued `SyncWorker` |
| `Skipped` | `now + interval` — never retried early |
| config missing or not initialised | alarm cancelled |

`Skipped` does not advance `lastUpdated`; a `Failed` attempt advances it only when at least one
screen applied. When nothing applied, `nextUpdate` is already in the past and arming it would tick
again immediately. A failure is also recorded in the config's `failure` (`target` + `at`), and
`cycleIfDue()` retries that target directly — so a partial apply (one screen succeeded, the other
failed) is not blocked by the advanced `lastUpdated`. The two outcomes are treated differently
because they age differently: a failed apply can clear on its own, so it retries within 1–5 minutes
and enqueues `SyncWorker` so a wallpaper deleted from disk is pruned before the retry picks again. A
skip is a condition only the user can clear — the album is gone, or every wallpaper in it is
blacklisted — so it waits a full interval instead of burning a wake-up every few minutes on
something that cannot change. `Skipped` deliberately does not enqueue `SyncWorker`: a permanently
skipped album would otherwise rescan its SAF folders on every cycle.

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

`onReceive` arms a provisional tick at `now + UpdateCoordinator.MAX_RETRY_DELAY` *before* calling
`goAsync()`. The alarm that fired has already been consumed, so without this a process death during
the update would leave nothing armed at all. `applyIfDue()` overwrites the provisional with the exact
deadline.

## FallbackWorker — the durability net

A 15-minute periodic `CoroutineWorker` whose whole body is `applyIfDue()`. It is not the timer and its imprecision does not matter: in the normal case it finds the cycle not due and does a single DataStore read.

It exists because an alarm lives in `system_server` and can be cleared without the app ever learning about it — OEM battery managers, force-stops, an update whose broadcast was missed. WorkManager persists its own state in a database and reschedules after a reboot, so this is the layer that cannot silently vanish. Both layers evaluate the same idempotent rule, so running them together is harmless.

## SyncWorker

Plain one-shot `CoroutineWorker` (unique work name, `KEEP`) that runs
`UpdateCoordinator.refresh()` — `AlbumSyncer.syncAlbums()` outside the mutex, then
`ConfigReconciler.reconcile()` under it. Enqueued by `UpdateCoordinator` after an applied or failed
cycle, and called directly by `WallmaticActivity.onCreate` and the Dev Tools sync action.

It is deliberately off the timed path: `syncAlbums()` scans SAF folders and can take seconds, while
a tick runs inside the ~10-second window a doze-exempt alarm grants a receiver. Applying a wallpaper
only needs Room, so the tick stays IO-bound. The reconcile half is cheap — Room and config only,
never the filesystem.

## WallpaperCycler and WallpaperApplier

`WallpaperCycler` owns selection, persistence and the cadence rule; `WallpaperApplier` owns the
Android half of an apply. The cycler never decodes a bitmap and the applier never reads config.

### WallpaperCycler (`apps/wallmatic/src/main/java/com/axondragonscale/wallmatic/core/WallpaperCycler.kt`)

- `cycleIfDue()`: reads config and honours the interval. Config missing or not initialised → `Skipped`. A recorded `failure` → `cycle(failure.target)` immediately. A non-null `nextUpdate` still in the future → `NotDue`; a null `nextUpdate` (never applied) is due. Otherwise it resolves the due target from the configured albums (`Home`, `Lock`, or `Both`) and delegates to `cycle(target)`; nothing configured → `Skipped`
- `cycle(target)`: loads the album(s), picks a random non-blacklisted wallpaper per applied screen and applies each through `WallpaperApplier.apply()`, then persists config for the screens that actually applied — `currentWallpaperId` per applied screen, `lastUpdated` as soon as one screen applied. Returns `Updated`, `Skipped(reason)` (config null / not initialised / album missing / album has no whitelisted wallpaper) or `Failed(cause)`. A missing file or a revoked URI grant is a failure, not a leaked descriptor
- An album whose whitelist is empty (every wallpaper blacklisted) is not a failure: `getRandomWallpaperOrNull()` returns `null`, that screen is skipped with a warning naming the album id, and the other screen is still applied. If no screen applied and nothing threw, the result is `Skipped(reason)`; a failure cause always wins over a skip reason, and `Failed` stays reserved for an apply that actually failed
- A failed apply is never persisted as a success: if nothing applied, `lastUpdated` stays unadvanced and a `WallpaperFailure` is recorded; if only some screens applied, the successes are persisted and `lastUpdated` advances, while `Failed` carries the first failure. A successful apply that resolves the failed target clears the record
- `applyWallpaper(wallpaper, target)` skips the random pick but otherwise records exactly like a cycle apply. Mirroring goes through one `effectiveTarget` rule, so any request — `Set Home` included — is applied and recorded as `Both`; Home and Lock cannot diverge. `reapply(wallpaper)` resolves the screens already showing it through `config.currentTargetFor()` and returns `NotDue` when it is not current anywhere
- When `mirrorHomeForLock = true`: one pick from the home album is applied to both screens, and every apply and record is `Both`
- The explicit `cycle(target)` bypasses the interval check — used by `UpdateCoordinator.applyNow()` for tiles, album changes and the failure row's `RETRY`
- A failed `applyWallpaper` returns `Failed` but does not record a `WallpaperFailure`, so the schedule keeps its existing deadline and does not retry that target

### WallpaperApplier

`apply(wallpaper, target)` returns a `Result` and never throws. It streams a wallpaper with no custom
crop and decodes one with a crop — both paths are in [Wallmatic context](./context.md#apply).
`applySize()` and `sourceSize()` expose the target bounds and the probed source dimensions for Dev
Tools.

## Quick Settings Tiles

Three `TileService`s for immediate manual wallpaper changes from the notification shade, all in
`apps/wallmatic/src/main/java/com/axondragonscale/wallmatic/background/TileServices.kt`. They extend
`TargetTileService`, which owns the tap handling and the injected `UpdateCoordinator`; each subclass
only declares its target.

| Service | Target | Icon |
|---|---|---|
| `WallmaticTileService` | Both screens | `ic_replace_image` |
| `HomeTileService` | Home only | `ic_home_replace_image` |
| `LockTileService` | Lock only | `ic_lock_replace_image` |

Tile taps call `UpdateCoordinator.applyNow(target)`: the interval check is bypassed, config is persisted for the applied screens, and the tick is re-armed from the new `lastUpdated` — so a manual change also pushes the next automatic one out by a full interval.

`Set Home` / `Set Lock` / `Set Both` in a wallpaper's long-press menu takes the same mutex-guarded
path through `UpdateCoordinator.applyWallpaper()`: the chosen wallpaper is applied immediately,
`currentWallpaperId` is written for the targets actually applied (`Both` writes both), and
`lastUpdated` advances before the tick is re-armed. With mirroring on, every target is applied and
recorded as `Both`. A failed apply is not recorded as current and, unlike a cycle failure, records no
`WallpaperFailure`, so the schedule keeps its deadline instead of retrying that target.

## Saving a crop

Saving in the editor's edit mode writes only the three crop columns, then asks the coordinator to
re-apply the wallpaper if it is already showing:

```
1. Wallpaper editor (edit mode) → UPDATE wallpaper SET focalX / focalY / zoom WHERE id
       ↓
2. UpdateCoordinator.reapply(wallpaper)               (mutex)
       ↓
3. Config.currentTargetFor(wallpaperId) → Home / Lock / Both, or null
       ↓
4. WallpaperCycler.applyWallpaper()     → applier.apply() + currentWallpaperId, lastUpdated
       ↓
5. armForResult(Updated)                → next tick a full interval away
```

`reapply()` takes the same mutex as `applyIfDue()`, so a crop save cannot race a scheduled apply.
`currentTargetFor` resolves the screens already showing that wallpaper and honours
`mirrorHomeForLock`, so a mirrored Lock is re-applied as `Both`. The apply is recorded like a cycle
apply — `currentWallpaperId` per screen and `lastUpdated` — and the schedule is re-armed from the new
timestamp. A wallpaper that is not current anywhere gets only its crop written; nothing is applied
and the existing tick stands.

## Data Flows

### Scheduled cycle

```
1. Alarm fires at lastUpdated + interval
       ↓
2. TickReceiver arms a provisional tick, then goAsync()
       ↓
3. UpdateCoordinator.applyIfDue()         (mutex)
       ↓
4. WallpaperCycler.cycleIfDue()           ← interval check, failure retry
       ↓
5. WallpaperApplier.apply() → persist currentWallpaperId + lastUpdated
       ↓
6. arm the next tick at the new nextUpdate, enqueue SyncWorker
```

If step 5 applies nothing, step 6 becomes the bounded retry instead, and `SyncWorker` is enqueued so the next attempt sees a pruned album.

### App start

```
1. WallmaticActivity.onCreate() → UpdateCoordinator.refresh()
       ↓
2. WallmaticActivity.onStart()  → UpdateCoordinator.applyIfDue()
```

`onStart` fires on first launch and on every return to the foreground, including after the system picker closes. `cycleIfDue()` honours the interval, so the normal case is a cheap `NotDue` no-op; an overdue cycle is applied instead of waiting for a deferred alarm. Syncing stays in `onCreate` because syncing in `onStart` races with the picker writing the same albums.

### Album or folder deleted

```
1. LibraryVM → WallmaticRepository.deleteFullAlbum()    album + folders + their wallpapers, one transaction
2. UpdateCoordinator.albumsChanged()                    (mutex)
3. ConfigReconciler.reconcile()                         clears that screen's album if the configured
                                                        album is gone, else re-applies a stale
                                                        current wallpaper
4. armForResult(result)                                 re-arms from the reconcile apply or the current nextUpdate
```

Deleting a folder from the album screen goes through `AlbumEditor.removeFolder`, which calls
`deleteFullFolder` to remove the folder and its wallpapers in one transaction. There is no
single-wallpaper delete action: `AlbumSyncer` prunes a wallpaper row when its file disappears, and
deletes a folder whose last wallpaper is gone. There are no dangling id lists to re-read and no cover
to recompute, because ownership lives on the child and covers are derived.

### Reboot / app update

```
1. BOOT_COMPLETED or MY_PACKAGE_REPLACED
       ↓
2. TickReceiver → applyIfDue() → tick re-armed, fallback re-ensured
```

A clock change needs no immediate update: the next run re-evaluates `lastUpdated + interval` itself.

## Permissions

```xml
<uses-permission android:name="android.permission.SET_WALLPAPER" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

No `SCHEDULE_EXACT_ALARM`: the tick uses an inexact doze-exempt alarm on purpose.

## Related Context

- [Wallmatic Context](./context.md) — Module overview, database schema, config schema
- [Core Modules](../../core/context.md) — Shared utilities
- [Main Context](../../context.md) — Project overview, tech stack
