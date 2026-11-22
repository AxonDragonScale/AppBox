---
title: "Logging"
status: current
summary: "The Logger fan-out: the platform logger plus optional sinks, and the dev-only Room store that keeps records after logcat rolls over."
read_when: "Adding a log destination, wiring dev-tools log capture in an app, or reading old logs."
---

> Context: [Main](../context.md) > [Core Modules](context.md) > Logging

# Logging

## One call, several destinations

`com.axondragonscale.commons.Logger` keeps its `expect`/`actual` platform dispatch (Android
`android.util.Log`, JVM and iOS `println`) and forwards every record to any registered
[`LogSink`](common/context.md) as well:

```kotlin
Logger.addSink(mySink)      // from now on every record reaches it too
Logger.removeSink(mySink)
```

A sink that throws is ignored, so a broken destination cannot silence the platform log or the other
sinks. Records arrive on the caller's thread; a sink that touches disk must queue.

`Logger.isLoggingEnabled` gates everything, sinks included.

## The dev-only store

The store and the reader are shared KMP code in `:core:devtools`; only opening the database and the
container around the reader are per platform:

| Platform | `installDevTools` |
|---|---|
| Android | `installDevTools(context)` — `dev_logs.db` in the app's databases directory |
| JVM | `installDevTools()` — `~/.appbox/dev_logs.db` |
| iOS | `installDevTools()` — `dev_logs.db` in the documents directory |

That is the module's single install call; the per-tool `installDevLogStore` behind it is not meant
to be called by a host.

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        installDevTools(this)   // a debug-source-set file that forwards to :core:devtools
        initKoin()
    }
}
```

Install before the first log worth keeping. The call itself does no I/O: it registers the sink
immediately, then builds the database, trims it and starts the writer on one background thread.
Records logged before the database is ready are dropped rather than buffered.

| Column | Meaning |
|---|---|
| `id` | Insertion order, the sort key |
| `timestamp` | `kotlin.time.Clock` epoch millis |
| `priority` | `LogPriority` name, e.g. `WARN` |
| `tag` | The logger's tag, without the `#` |
| `message` | The full message, untruncated |
| `session` | A random id per process, so one run can be told from another |

Writing is asynchronous: `Logger` enqueues into a bounded channel (1,000 records, oldest dropped)
and the writer inserts whatever is queued, up to 100 at a time. A lone record is inserted as soon as
it arrives; a burst becomes one insert. Insert failures are swallowed — logging a log failure would
feed the same sink — so a broken database shows an empty reader rather than a loop.

The newest 20,000 rows are kept, trimmed once when the store is installed.

## Reading them

- **Dev Tools → Open Logs** — the shared
  [log viewer](../libraries/log-viewer/context.md), newest first, filterable by level, text and tag
  rules, with the full message on tap, infinite-scroll history, share, and clear. It appears only
  when the store was installed, so an app that never installs it has no Logs entry.
- **Dev Tools → Open Storage** (Android) — the same database as a plain Room database.

Paging is keyset-based (`id < beforeId`), so records arriving while you scroll cannot duplicate rows.
Records are stored and read whole; the viewer trims the row to its first line and shows the rest in
the detail. **Share** exports exactly the rows on screen.

Both the reader and the database survive process death, reboots and logcat's own rollover, which is
the point: an agent can read what a previous session logged.

## Scope

Only dev build types keep `:core:devtools`, so only they open the store. A release build has neither
the database nor the reader; see [Dev tools in release](context.md#dev-tools-in-release).

## Related Context

- [Core Common](common/context.md) — `Logger` and the `LogSink` seam
- [Core Modules](context.md) — DevTools, which owns the store and the reader
- [Build System](../build-system.md) — `appBox { }` decides which build types keep them
