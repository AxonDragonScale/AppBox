---
title: "Log viewer library"
status: current
summary: "A KMP Compose log console over a LogSource: logcat on Android, the dev log database everywhere, and any host-supplied source."
read_when: "Changing the log viewer UI, the source contract, or adding a log source."
---

> Context: [Main](../../context.md) > Libraries > Log Viewer

# Log Viewer — `:libraries:log-viewer`

## Overview

A log console a host can drop into a debug build. It asks its source for records instead of parsing
a stream, so the same UI reads `logcat` on Android and the dev log database on every platform.

**Package**: `com.axondragonscale.logviewer` · **Targets**: Android, JVM, iOS (Compose Multiplatform)

```text
libraries/log-viewer/src/
├── commonMain/.../logviewer/
│   ├── LogViewer.kt              the public entry composable
│   ├── model/                    LogLevel, LogRecord, LogId, LogMeta
│   ├── source/                   the contract, LogQuery/rules, InMemoryLogSource
│   └── ui/                       screen, ViewModel, format, chrome/, filter/, list/, sheets/, style/
├── androidMain/.../logviewer/
│   ├── logcat/                   LogcatSource + LogcatScreen (permission gate included)
│   └── ui/                       LogBackHandler.android.kt
└── jvmMain, iosMain              LogBackHandler actuals
```

## The contract

```kotlin
interface LogSource {
    val capabilities: LogCapabilities
    val key: String                                // the stream's identity in a ViewModelStore
    suspend fun facets(): LogFacets = LogFacets()   // the values the pickers can suggest
    fun open(query: LogQuery): LogStream
}

interface LogStream : AutoCloseable {
    val entries: StateFlow<List<LogRecord>>   // newest first
    val isLoaded: StateFlow<Boolean>          // false until the first page is read
    val hasOlder: StateFlow<Boolean>
    val notice: Flow<String?>                 // a one-line reason the source cannot deliver
    suspend fun loadOlder()
    suspend fun clear()
}
```

- The **UI asks for what it renders**: a query in, paged records out. The source applies the filter
  however it can — SQL, an in-memory buffer, a remote call. Records stay newest → oldest with stable
  ids; a bounded source (logcat's 5,000-row buffer) may drop its oldest row, so the viewer anchors on
  the previous head and its first visible key rather than on list size or the tail.
- `LogQuery` carries levels, text and a flat list of `LogRule`s. Each rule names a `LogFacet`
  (`TAG`, `PACKAGE`), a value, and whether it keeps or drops it, so a new facet is one enum entry
  rather than another field on every query, source and screen.
- `LogCapabilities` is how a source says what it has: `live`, `history`, `pid`, `tid`,
  `packageName`, `sessions`, `clear`. The viewer hides everything else — no "load older" without
  `history`, no clear action without `clear`, no meta fields a source does not have.
- `LogRecord` keeps the **full message**. The row renders its first line; the detail sheet shows the
  whole thing.
- `LogMeta` is the optional half: `pid`, `tid`, `packageName`, `session`. `null` means
  the source does not have the field; the UI does not invent one.

Levels are canonical — `TRACE, DEBUG, INFO, WARN, ERROR, FATAL` — and every source maps into them:

| Source | Mapping |
|---|---|
| logcat | `V D I W E A` → `TRACE … FATAL` |
| `LogPriority` | `VERBOSE … ASSERT` → `TRACE … FATAL` |
| os_log | `debug info default error fault` → `DEBUG INFO INFO ERROR FATAL` |

The source's own spelling is folded into that ramp; no raw letter is kept.

## Android — `LogcatSource`

```kotlin
val source = LogcatSource(context)
LogViewer(source = source, onBack = …)
```

- Runs `logcat -v threadtime,year` as the calling UID — no permission and no helper app. `year` is
  what gives the viewer a full date; plain `threadtime` has none.
- Asks for **all buffers** (`-b all`). A device that refuses it falls back to the default buffers
  once; there is no buffer picker.
- Parses `YYYY-MM-DD HH:MM:SS.mmm PID TID L TAG: message`, merges continuation lines and stack
  frames into the record above, and keeps the newest 5,000 records.
- Reports `history = false` — the buffer is all there is, so the viewer never offers older pages.
- Resolves `pid` → package through `ActivityManager`, falling back to `/proc/<pid>/cmdline`, for the
  detail sheet's meta line.
- One reader lives for the source's lifetime; a filter change only rebuilds the view. Restarting
  `logcat` per keystroke would re-trigger the platform's log-access prompt every time.
- Android 17 (API 37) gates system logs twice. The permission is granted once over ADB
  (`adb shell pm grant <pkg> android.permission.READ_LOGS`), which the **Read logs** screen prints;
  the first read then asks the user to allow all-device logs.

`LogcatScreen(onBack, onShare, onCopy)` is that source, its permission gate and its viewer in one:
without the grant it prints the command instead of an empty list, and it re-checks whenever the app
returns. A host that wants the console as a navigation destination calls it directly.

## The database — `StoredLogSource`

`:core:devtools` implements the same contract over its Room store: keyset pages for history, a 2 s
poll for the live tail, `clear` over the database itself, `sessions = true`. A database that cannot
be opened or read shows up as a `notice` line rather than as an empty list. See
[Logging](../../core/logging.md).

## What the screen does

- **Newest at the bottom**, like a console. At the bottom, new records appear as they arrive. Scroll
  away and the list holds the row you are reading; the jump arrow highlights and returns to the
  newest. A filter change starts following again.
- **Reaching the end loads the next page** — no button. The footer item triggers the load when it
  is composed; a source without history never renders a footer at all.
- **Filters**: a search field and a level chip on the bar, and a `Tune` sheet split into Tag and
  Package sections. Each section has filled `+ Include` and `− Exclude` buttons that open a picker
  sheet — a scrolling list of the source's tags or packages plus a field for a name it doesn't have.
  The sheet closes with `Show N lines`. Applied rules sit on the bar as removable chips; an
  exclusion reads `-tag` on the warning surface, packages read `pkg:name`.
- **Detail**: tapping a row opens a bottom sheet with the level badge, the tag, the full date and
  time, the message and its trace block, pills for the meta the source has, and Copy / Include /
  Exclude / Share actions plus the rule that explains them. Copy and share are host callbacks, so
  the common code touches no platform clipboard.
- **Pause** freezes the list and counts what arrives; the jump action returns to the newest. The
  overflow menu holds **Wrap long lines**, **Share logs** and, where the source supports it,
  **Clear logs**.
- **Share logs** (the overflow's action) exports the records the viewer has loaded.

The anchors are explicit, not inferred: the viewer reads its first visible key and offset **before**
applying a new list, then re-anchors on that key. That is what keeps a 200-record live batch from
moving the viewport.

## Public API

```kotlin
@Composable
fun LogViewer(
    source: LogSource,
    modifier: Modifier = Modifier,
    caption: String = "",          // status line tail, e.g. "all buffers"
    title: String = "Logs",
    onBack: (() -> Unit)? = null,
    onShare: ((String) -> Unit)? = null,
    onCopy: ((String) -> Unit)? = null,
)
```

`onBack == null` hides the back arrow; `onShare == null` hides share. The viewer renders in the
ambient `MaterialTheme`; DevTools hosts it inside `DevToolsTheme`, so it looks the same everywhere.

A closed viewer releases its stream but keeps its filters. A logcat stream is a view over the
source's one reader, so closing it leaves the process alone; the DevTools host closes the source —
and its `logcat` process — when the screen goes away. The ViewModel is keyed by `LogSource.key`
(the class name unless a source overrides it), so the Logs and Logcat dialogs never share one.

## Hosts

- **DevTools** on Android: **Open Logcat** (`LogcatSource`) and **Open Logs** (`StoredLogSource`),
  both as full-screen dialogs.
- **DevTools** on JVM and iOS: **Open Logs** only, since there is no logcat there.

## Removed with the rename

`ILogcatSource` and its `InputStream` protocol, the buffer picker, saved views and regex search are
gone. A package filter came back on top of `LogQuery` once the viewer needed it, which is why rules
are one list of `LogRule` rather than parallel included/excluded sets per facet. `log-viewer.fig`
draws the logcat chrome and the shipped states — the level menu, both sheets, the rule picker, the
overflow and the gate — so the board and the library move together.

## Building

```bash
./gradlew :libraries:log-viewer:assemble
```

Previews: `android preview list :libraries:log-viewer`, then
`android preview render :libraries:log-viewer --fqn=<fqn> -o <dir>`.

## Related Context

- [Main Context](../../context.md) — project overview, module index
- [Logging](../../core/logging.md) — the dev log store behind `StoredLogSource`
- [Coding conventions](../../coding-conventions.md)
