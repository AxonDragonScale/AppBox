---
title: "Verso — reader app"
status: in-progress
summary: "Android reader for EPUB/FB2/TXT/MD/HTML with read-aloud, focus mode and bionic reading. Scaffold only: module, shell, tokens, empty library."
read_when: "Working in apps/verso, or checking how much of the reader actually exists."
---

> Context: [Main](../../context.md) > Verso

# Verso — Reader App

Behaviour: [spec](./spec.md). Visuals: [UI](./ui.md).

## Overview

A local-first reading app built around read-aloud, a low-eye-movement focus mode and bionic reading.

- **Package**: `com.axondragonscale.verso`
- **Module path**: `:apps:verso`
- **Platform**: Android only, `minSdk 31` / `compileSdk 37` from the app convention plugin
- **Network**: none. The manifest carries no `INTERNET` permission; voice-model downloads will add it
  when they exist.

## What exists

- **Shell**: `VersoActivity` (edge-to-edge, splash), `VersoApplication` with an empty Koin graph,
  type-safe routes in `Route`, and a three-segment tab bar over Library, Stats and Settings.
- **Theme**: the palette (`paper`, `paperSunk`, `ink`, `inkMuted`, `rule`, `highlight`, `selection`),
  the type scale and the spacing tokens from the UI doc. Square everywhere; sheets alone are rounded.
- **Design board**: `apps/verso/verso.fig` — Foundations complete, a four-batch component kit, and
  twenty-three built screens: the reader chrome, Library (empty, grid, list, loading, scope and filter
  states), Stats, Settings, book detail in three states, and the four import states.
- **Library**: the empty state only — header, formats line, `Import books` / `Import a folder`,
  `Books stay on this device.` Both actions are unwired.
- **Stats / Settings**: honest placeholders, no rows, no fake data.

## Not built yet

Import, book storage, format parsing, pagination and the reader surface, read-aloud and voice
management, focus mode, bionic reading, bookmarks, dictionary and translation, stats, export/import.

## Structure

```
com.axondragonscale.verso/
├── VersoActivity.kt          # Edge-to-edge entry point
├── VersoApplication.kt       # Koin startup
├── di/VersoModule.kt         # Empty until the first repository exists
└── ui/
    ├── VersoApp.kt           # Scaffold + NavHost + tab bar
    ├── Route.kt              # @Serializable destinations
    ├── component/            # ActionButton, VersoTabBar
    ├── library/              # Empty state
    ├── stats/                # Placeholder
    ├── settings/             # Placeholder
    └── theme/                # Color, Theme, Type, Dimens
```

## Invariants

- **Colour belongs to the voice.** `highlight` is the only warm colour in the palette; selection is
  grey. Nothing else may introduce an accent.
- **Square by default**, no elevation, no shadows — `VersoShapes` encodes it.
- **No fake data.** A screen either shows real state or says plainly that there is none yet.
- Fonts are system stand-ins until Fraunces, Inter and Literata are bundled; sizes and metrics already
  match the UI doc, so bundling changes no layout.

## Commands

```bash
./gradlew :apps:verso:assembleDebug
```
