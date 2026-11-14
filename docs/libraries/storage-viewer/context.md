---
title: "Storage viewer library"
status: in-progress
summary: "Scaffolded Android library for browsing the app's own databases, preferences and files, with a three-page design board behind a 29-screen viewer; no source yet."
read_when: "Wiring the module, reviewing the design board, or starting the storage viewer implementation."
---

> Context: [Main](../../context.md) > Storage Viewer Library

# Storage Viewer — `:libraries:storage-viewer`

## Overview

Android + Compose library that will show the calling app its own storage:

- **Databases** — SQLite/Room files discovered through `Context.databaseList()` and the
  `databases/` directory, browsed read-only in place.
- **Preferences** — Preferences DataStore files (`files/datastore/*.preferences_pb`) and
  SharedPreferences XML, read without constructing a second `DataStore`.
- **Files** — app files, cache, no-backup files, external cache and code cache, browsed read-only,
  with capped text and binary previews.

Typed proto DataStore files are out of scope. Reads happen against the live files; the library
makes no snapshots or copies.

## Current state

The Gradle module is registered and builds an empty AAR. There is no source yet: no
`StorageViewer` composable, no discovery, no readers.

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

## Planned shape

```text
libraries/storage-viewer/src/main/java/com/axondragonscale/storageviewer/
├── StorageViewer.kt              # public entry: the three store tabs
├── model/                        # descriptors, schema, rows, preference entries, file nodes
├── data/                         # IStorageSource, AndroidStorageSource, readers, file walker
└── ui/                           # home, browser screens, sheets, states
```

`:core:devtools` will add an Android-only **Open Storage** built-in, mirroring the logcat host
seam (`isStorageViewerAvailable` / `DevToolsStorageHost`, JVM and iOS no-ops).

## Building

```bash
./gradlew :libraries:storage-viewer:assembleDebug
```

## Related Context

- [Main Context](../../context.md) — project overview, module index
- [Design](./design.md) — screens, states, components, tokens and copy
- [Core Modules](../../core/context.md) — devtools and its built-ins
- [Logcat Viewer](../logcat-viewer/context.md) — the sibling viewer library
- [Coding Conventions](../../coding-conventions.md)
- [Build System](../../build-system.md)
