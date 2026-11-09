---
title: "Logcat viewer library"
status: current
summary: "Compose log viewer an app can embed to read its own records, with a host-supplied ILogcatSource and no permission gate."
read_when: "Changing the viewer UI, the log source contract, or adding a host."
---

> Context: [Main](../../context.md) > Logcat Viewer Library

# Logcat Viewer — `:libraries:logcat-viewer`

## Overview

An Android + Compose log viewer an app can drop into a debug (or any non-production) variant to read
**its own** log records. It has no permission gate, no helper app and no dependency on Devinator's
theme or DI: the default source simply runs `logcat` as the calling UID, which the platform has
allowed since API 16.

A host that holds a broader identity — root, a shell bridge, a remote device — implements
`ILogcatSource` and passes it in. The viewer's UI does not change.

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin (JVM 21) |
| Min SDK | 31 |
| UI | Jetpack Compose + Material 3 (BOM) |
| DI | none in the library; `LogcatViewModel.factory(...)` |
| Persistence | Preferences DataStore (the viewer's own `logcat_viewer` file) |
| Serialization | kotlinx.serialization (filter snapshot types) |

## Project Structure

```
libraries/logcat-viewer/
├── build.gradle.kts
├── consumer-rules.pro
├── proguard-rules.pro
└── src/main/java/com/axondragonscale/logcatviewer/
    ├── LogcatViewer.kt              # public entry: wiring, lifecycle, effects, style
    ├── model/
    │   ├── LogEntry.kt              # LogLevel, LogBuffer, LogEntry
    │   ├── LogFilter.kt             # FilterKind, FilterRule, LogFilter
    │   └── SavedView.kt
    ├── data/
    │   ├── ILogcatSource.kt         # ILogcatSource + ILogcatProcess seam
    │   ├── ProcessLogcatSource.kt   # default: ProcessBuilder("logcat", "-v", "threadtime")
    │   ├── LogcatPackageResolver.kt # package → live PIDs, pid → package (internal)
    │   ├── ISavedViewsStore.kt      # interface + list transforms
    │   └── DataStoreSavedViewsStore.kt # the default persistence
    ├── stream/
    │   └── LogcatStreamer.kt        # reads, parses, merges traces, batches every 120 ms
    └── ui/
        ├── LogcatScreen.kt          # scaffold, list, sheet and dialog hosts
        ├── LogcatViewModel.kt       # UI state, events, effects, filtering, package PIDs
        ├── chrome/                  # the bar, its menus and the dialogs it opens
        │   ├── LogcatTopBar.kt
        │   ├── LogcatMenu.kt        # menu surface, selectable row, overflow and buffer menus
        │   └── LogcatDialogs.kt     # name, delete and add-rule dialogs
        ├── filter/                  # everything that narrows the list
        │   ├── LogcatFilterBar.kt   # the two filter rows and the add-rule draft
        │   ├── LogcatSearchField.kt # text field + regex chip (page and sheet)
        │   ├── LogcatLevelMenu.kt   # level filter button and menu
        │   └── LogcatRuleChip.kt    # applied-rule chip, None chip, rule action mapping
        ├── list/
        │   ├── LogcatEntryList.kt   # the list and one row
        │   ├── LogcatStates.kt      # paused banner and empty state
        │   └── LogcatTrace.kt       # one trace renderer for the row and the sheet
        ├── preview/
        │   └── LogcatPreviews.kt    # one palette wrapper and one fixture set
        ├── sheets/                  # entry/filter/saved-view sheets + shared sheet chrome
        └── style/
            └── LogcatStyle.kt       # public tokens, level colours, divider
```

Everything under `ui/` and `LogcatStreamer` is `internal`: the library's surface is `LogcatViewer`,
`ui.style.LogcatStyle`, the model types, `ILogcatSource` / `ProcessLogcatSource`, and
`ISavedViewsStore` / `DataStoreSavedViewsStore`.

## Public API

```kotlin
@Composable
fun LogcatViewer(
    modifier: Modifier = Modifier,
    source: ILogcatSource = remember { ProcessLogcatSource() },
    savedViewsStore: ISavedViewsStore? = null,
    title: String = "Logcat",
    style: LogcatStyle = rememberLogcatStyle(),
    onBack: (() -> Unit)? = null,
    onShare: ((String) -> Unit)? = null,
)
```

- `savedViewsStore == null` uses `DataStoreSavedViewsStore`; a host only passes one to move the
  saved views somewhere else.

- `onBack == null` hides the navigation icon.
- `onShare == null` falls back to an `ACTION_SEND` chooser opened with the passed context.
- Snackbars are drawn inside the viewer; there is no host callback for them.

The host owns the theme (`MaterialTheme`). The viewer reads `MaterialTheme.colorScheme` and adds only
what Material has no role for, through `LogcatStyle`:

| Field | Used for | Default |
|---|---|---|
| `mono` | log lines, meta chips | 12/16 monospace |
| `action` | step actions, inline links | 13/600 |
| `sectionTitle` | sheet titles | 20/600 |
| `debug`, `warn` | `D` and `W` glyphs | blue / amber |
| `warnSurface`, `warnRowTint` | warning chips and rows | amber tint |
| `errorRowTint`, `traceSurface` | error rows and inline traces | error tint |

A host overrides it by passing its own [LogcatStyle]; `rememberLogcatStyle()` is the default that
follows the resolved Material scheme, so a forced light or dark app gets the matching ramp.

## Where the lines come from

`ProcessLogcatSource` runs `logcat -v threadtime` with **no** `-T` (a count would stop following) and
no `-b` unless a buffer is chosen (`-b all` fails on the test device's logd; omitting the flag reads
the default buffers). A host-supplied `ILogcatSource` must emit that same `threadtime` shape, because
the reader parses `MM-DD HH:MM:SS.mmm  PID  TID L TAG: message` and treats every other line as a
continuation of the record above.

The process runs as the app's UID. Verified on the iQOO (Android 17):

- With `READ_LOGS` granted: `run-as <pkg> logcat` returns only this app's records.
- With `READ_LOGS` revoked: still this app's records, uninterrupted.
- `adb shell logcat` (shell UID) returns every process.

So the grant changes nothing for an app UID, and the library asks for none. The consequence is
honest and visible: the viewer shows this app's logs and the system's records about it, not other
apps.

## Stream behaviour

- `LogcatStreamer.start(buffer)` returns a `LogSession` token; `stop(token)` ignores a stale owner, so
  a cleared ViewModel cannot kill a stream a newer screen started.
- The shown entries outlive the process. The viewer starts the reader on `ON_START` and stops it on
  `ON_STOP`, because the platform stops delivering records to an app's `logcat` child once the app is
  backgrounded. Starting the same buffer again resumes after the newest shown record — a replay guard
  drops the seeded lines at or before it — so returning fills the gap without repeating history.
  Switching buffers is a new view and starts empty. This was verified on an iQOO (Android 17).
- Lines arrive on a reader coroutine, are parsed against
  `MM-DD HH:MM:SS.mmm  PID  TID L TAG: message`, and published as a snapshot every 120 ms — not per
  line. At most 5,000 entries; one message is capped at 16k characters.
- Continuation lines and stack frames/`AndroidRuntime` crash blocks merge into the entry above when
  pid, tid and tag match, so one exception is one row.
- `--------- beginning of …` separators are dropped.
- Pause keeps reading into a pending queue; resume appends it. Clear empties the view, never the
  system buffer.
- Standard error is drained on its own coroutine so `logcat` cannot block on a full pipe.
- `InterruptedIOException` from the expected close after `process.destroy()` is not an error: the
  reader rethrows `CancellationException` and only logs when the coroutine is still active.
- A reader that produced lines and then exited is restarted up to three times, resuming from the
  same cursor; a reader that produced nothing is left stopped.

## Filter model

`LogFilter` holds levels, include/exclude tags, include/exclude packages, the content query and the
regex flag. Every mutation goes through a `FilterRule` (kind + value + polarity) and
`LogFilter.withRule` / `withoutRule`, so the sheet, the chips, the saved-view summary and the events
share one vocabulary. Everything is ANDed:

- **Levels** — multi-select; deselecting all is an honest empty state.
- **Content** — case-insensitive `contains` over the message, or `Regex` with `IGNORE_CASE` when the
  `.*` chip is on. An invalid pattern matches nothing.
- **Tags** — exact equality.
- **Packages** — resolved to live PIDs through `PackageManager` + `ActivityManager`
  `getRunningAppProcesses` inside the filter pipeline, refreshed every 3 s while a package rule is
  active; changing the filter cancels that refresh and starts it for the new rules. A package the
  platform hides resolves to no PIDs and shows no matches; the app's own package always resolves.

`LogFilter.summary()` and `levelLabel` are the only copy the UI composes from state.

## Saved views

`ISavedViewsStore` is the seam: `views: Flow<List<SavedView>>`, `save`, `rename`, `delete`.
`DataStoreSavedViewsStore` is the default and the viewer passes it itself, so an app needs no
wiring: it owns the `logcat_viewer` Preferences DataStore and stores the list as JSON under
`saved_views`. Writes go through `DataStore.edit`, so overlapping calls cannot lose a change. The
list transforms (`saving`, `renaming`, `removing`) live beside the interface so every store applies
the same semantics. A host that wants
the views elsewhere implements the interface over its own store; `SavedView` and `LogFilter` are
`@Serializable` so JSON storage needs no mapping.

## Known limitations

- Only this app's records are visible without a host-supplied `ILogcatSource`.
- ColorOS/OriginOS may hide other processes from `getRunningAppProcesses`, so package filters for
  other apps can resolve to nothing even with a broader source.

## Building

```bash
./gradlew :libraries:logcat-viewer:assembleDebug
```

Previews: `android preview list :libraries:logcat-viewer`, then
`android preview render :libraries:logcat-viewer --fqn=<fqn> -o <dir>` (light and dark).

## Design source

Devinator's board `Logcat · Console board` in `apps/devinator/devinator.fig` is the visual reference.
Its **no access** frame predates the decision to read only own logs and has no shipped counterpart.

## Related Context

- [Main Context](../../context.md) — project overview, module index
- [Devinator context](../../apps/devinator/context.md) — the first consumer
- [Coding conventions](../../coding-conventions.md)
- [Build system](../../build-system.md)
