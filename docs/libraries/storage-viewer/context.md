---
title: "Storage viewer library"
status: current
summary: "Android + Compose library that shows the app its own databases, preferences and files in place, with a public StorageViewer, an IStorageSource seam and a DevTools entry."
read_when: "Wiring the module, using StorageViewer in an app, reviewing the design board, or changing how storage is read."
---

> Context: [Main](../../context.md) > Storage Viewer Library

# Storage Viewer — `:libraries:storage-viewer`

## Overview

Android + Compose library that shows the calling app its own storage:

- **Databases** — SQLite/Room files discovered through `Context.databaseList()`, browsed
  read-only in place.
- **Preferences** — Preferences DataStore files (`files/datastore/*.preferences_pb`) and
  SharedPreferences XML, read without constructing a second `DataStore`.
- **Files** — app files, cache, no-backup files, external cache and code cache, browsed read-only,
  with capped text and binary previews.

Typed proto DataStore files are out of scope. Reads happen against the live files; the library
makes no snapshots or copies.

## Current state

The module builds. Its whole public surface is one composable:

```kotlin
StorageViewer(
    modifier = Modifier,
    source = null,                  // IStorageSource; null means AndroidStorageSource(context)
    title = "Storage",
    style = rememberStorageViewerStyle(),
    onBack = null,
)
```

Reads go through `IStorageSource` (`data/IStorageSource.kt`): `stores()`, then `describe`,
`tables`, `schema`, `rows`, `entries`, `children` and `preview`, each returning
`Result<…, StorageError>` instead of throwing because a store can vanish or lock between a
listing and an open. `AndroidStorageSource(context)` is the default implementation and reads the
calling app through `Context`; a host that wants another scope passes its own source.

The viewer renders in whatever `MaterialTheme` it is composed in; DevTools hosts it inside
`DevToolsTheme` from `:core:devtools`, so it does not inherit the host app's theme, dynamic colour or
light/dark choice. The library applies no theme of its own and takes no `:core:ui` dependency:
`rememberStorageViewerStyle()` returns the fixed dark palette as plain constants, with no read of the
ambient theme.

The three surfaces behind that seam:

- `ui/screens/StoresScreen.kt` — the segmented home: Databases, Preferences, Files.
- `ui/screens/TablesScreen.kt`, `SchemaScreen.kt`, `RowsScreen.kt` — database browsing, with the
  locked/retry state and an ordinal id for views and WITHOUT ROWID tables that reject `rowid`.
- `ui/screens/KeysScreen.kt`, `FolderScreen.kt`, `FilePreviewScreen.kt` — store keys, folder
  listing across the five roots, and capped text/binary file previews.

Readers live in `data/` (`Discovery`, `DatabaseReader`, `DataStoreReader`, `SharedPrefsReader`,
`FileWalker`, `ByteFormat`, `DisplayText`), descriptors and display models in `model/`, and the
component set, sheets and `StorageViewerStyle` in `ui/`.

`StorageViewModel` keeps the walk in a `SavedStateHandle`: the active tab, the home and detail
filters, and the depth as one route string — `stores`, `tables:<db path>`, `schema:<db path>|<table>`,
`rows:<db path>|<table>`, `keys:<store path>`, `folder:<root kind>:<path>`, `file:<root kind>:<path>`.
The route is a string because it is restored before the store scan exists and can only be matched by
path against the fresh scan; a store that is gone leaves the viewer at the store list. After the
scan the saved depth is re-entered and its read re-run, so an activity recreation or process death
returns to the same screen. Open sheets are not saved and never reopen.

## Design

`libraries/storage-viewer/storage-viewer.fig` is the visual reference, built with the same three
pages as the other library boards:

- **Foundations** — cover, the Dark/Light token collections (base, accent, status, store-kind and
  value-kind roles), typography, shape/spacing, iconography, and the storage/value ramp.
- **Components** — 420-wide specimens for the component set in [design.md](./design.md): store rows,
  segmented tabs, search, table, schema and file rows, key/value rows, chips, banners, empty state,
  top bars, path line, sheets and section header.
- **Screens** — a 1428-wide board with 29 412 × 915 screens in flow order plus five 396-wide sheet
  overlays: stores (databases, preferences, files, empty, no matches, loading), database tables
  (content, empty, busy), schema, rows (content, no matches), table empty, unreadable, store keys
  (content, empty, no matches), preferences search, files folder (content, empty, unreadable), file
  previews (text, binary, truncated), and four light counterparts (stores, rows, keys, files).

The board shows the agreed behaviour: read-only, in-place reads, no copies, values shown verbatim,
a retry state when a database is locked, and a files surface with five roots and no cache clear.
[design.md](./design.md) is the durable spec: screen inventory, state coverage, flows, components,
tokens and copy.

Typography uses Roboto Regular/Medium/Bold and Roboto Mono Regular. The boards avoid Roboto
SemiBold and Roboto Mono Medium because those faces render 1 px vertical lines at text ends in this
renderer; the variable Roboto fonts stay installed and the design uses only the clean faces.

## Module shape

```text
libraries/storage-viewer/src/main/java/com/axondragonscale/storageviewer/
├── StorageViewer.kt              # public entry: style, source, clipboard, effects
├── model/                        # descriptors, schema, rows, preference entries, file nodes, errors
├── data/                         # IStorageSource, AndroidStorageSource, readers, file walker
└── ui/                           # screens, sheets, list rows, chrome, style
```

`:core:devtools` has the Android-only **Open Storage** built-in, mirroring the logcat host seam:
`isStorageViewerAvailable` / `DevToolsStorageHost` in `commonMain`, a `StorageViewer` dialog in
`androidMain` with a `StorageDevToolsScreen(onBack)` navigation variant, and no-op actuals on JVM
and iOS. The library is an `androidMain` dependency only, so it never reaches the common
classpath. See [Core Modules](../../core/context.md).

## Building

```bash
./gradlew :libraries:storage-viewer:assembleDebug
```

## Related Context

- [Main Context](../../context.md) — project overview, module index
- [Design](./design.md) — screens, states, components, tokens and copy
- [Core Modules](../../core/context.md) — devtools and its built-ins
- [Log Viewer](../log-viewer/context.md) — the sibling viewer library
- [Coding Conventions](../../coding-conventions.md)
- [Build System](../../build-system.md)
