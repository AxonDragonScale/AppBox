> Context: [Main](../../context.md) > [Jest](./context.md) > UI Revamp Plan

# Jest UI Revamp — Responsive Redesign Plan

## Summary

Revamp the Jest app UI from a mobile-only layout to a responsive, multi-form-factor design based on the `jest.pen` design file. The new design introduces three layout tiers (mobile, tablet, desktop), replaces the HorizontalPager + floating bottom tab bar with an adaptive navigation pattern (bottom bar on mobile, sidebar on tablet/desktop), introduces a new custom dark theme (with a light variant) using Fraunces + DM Sans fonts, and restructures all three screens (Home, Library, Settings).

**Status:** Implementation in progress — Phases 0–6 substantially complete. Blocked on `WindowWidthSizeClass` resolution; switching to `adaptive` library for window size. Phase 7 (cleanup) not yet started.

---

## 1. Design Analysis — What the `.pen` File Shows

### Form Factors

| Tier | Breakpoint | Navigation | Content Layout |
|------|-----------|------------|----------------|
| **Mobile** | Compact (`< 600dp`) | Bottom tab bar (floating, 3 tabs: Home, Library, Settings) | Single column, full-width cards |
| **Tablet** | Medium (`600–1199dp`) | Fixed sidebar (240dp), 3 nav items: Home, Library, Settings | Single column with wider content area, 2-col grids |
| **Desktop** | Expanded (`≥ 1200dp`) | Fixed sidebar (280dp), same 3 nav items | Multi-column: main content + right panel on Home; grid layouts on Library/Settings |

### Destinations (Changed from 4 tabs → 3 destinations)

**Current**: 4 tabs — Home, Favorites, History, Settings
**New**: 3 destinations — Home, Library (merges Favorites + History), Settings

### Theme / Color System

The design uses a fully custom dark theme. A light variant will also be created. Dynamic theme (Material You) toggle is retained in Settings.

#### Dark Theme Tokens (from design)

| Token | Value | Usage |
|-------|-------|-------|
| Background | `#0B0B0E` | App background |
| Surface / Card | `#16161A` | Cards, elevated surfaces |
| Border / Divider | `#2A2A2E` | Subtle borders, dividers |
| Primary accent | `#6366F1` (Indigo) | Active nav items, category badges, CTA buttons |
| Secondary accent | `#FFB547` (Amber) | Settings nav highlight, day streak stat |
| Error/Favorite | `#E85A4F` | Favorite hearts, dark category badge |
| Success | `#32D583` | Pun category badge, jokes seen stat |
| Text primary | `#FAFAF9` | Headings, body text |
| Text secondary | `#4A4A50` | Inactive nav, placeholder text |
| Text tertiary | `#6B6B70` | Section labels (APPEARANCE, ABOUT, etc.) |

#### Typography

| Role | Font | Usage |
|------|------|-------|
| Display / Heading | `Fraunces` | App title, joke text, section headers |
| Body / UI | `DM Sans` | Nav labels, chips, stats, settings rows |

### Screen-by-Screen Design

#### Home Screen
- **Header**: Simple "Jest" text with subtitle (mobile top bar) or sidebar branding (tablet/desktop)
- **Filter chips row**: "Category" and "Flags" filter chips — bottom sheet (mobile) or custom popover (tablet/desktop)
- **Hero joke card**: Dark card (`#16161A`, 20dp corners):
  - Top row: Category badge (colored pill) + Favorite button
  - Joke setup text (Fraunces, white)
  - Joke punchline text (Fraunces, indigo `#6366F1`)
  - Bottom row: Share button + "Next Joke" CTA button (indigo filled)
- **Stats row**: 3 stat cards (Jokes Seen, Favorites, Day Streak) — colored numbers
- **Recent Jokes section**: DEFERRED — not included in initial implementation

#### Library Screen (replaces Favorites + History)
- **Header**: "Library" title
- **Filter chips**: Same Category/Flags chips
- **Joke cards grid**: `LibraryJokeCard` component — joke text, category badge, timestamp, favorite + share actions
  - Mobile: Single column
  - Tablet: 2-column grid
  - Desktop: 2-column grid with more breathing room
- **Filter toggle**: All vs Favorites (chip or tab within Library)

#### Settings Screen
- **Sections**: APPEARANCE, ABOUT, DEV TOOLS
- **Layout**:
  - Mobile: Single column, stacked sections
  - Tablet: Two-column layout (Appearance left, About + DevTools right)
  - Desktop: Three-column layout (one section per column)
- **Components**: `SettingsToggleRow`, `SettingsLinkRow`, `SettingsDivider` — dark card surfaces
- **Dynamic theme toggle retained**

---

## 2. Aligned Architectural Decisions

### Decision 1: Window Size Classification — `adaptive` library ✅ (UPDATED)

~~Use `org.jetbrains.compose.material3:material3-window-size-class`~~ — **this approach was attempted but abandoned.**

**Root cause:** `WindowWidthSizeClass.COMPACT / MEDIUM / EXPANDED` companion object members were unresolvable in `commonMain` at compile time despite the library appearing in the jvm classpath. The `calculateWindowSizeClass()` function is also explicitly NOT available in common code per CMP 1.7.3 release notes.

**New approach:** Use `org.jetbrains.compose.material3.adaptive:adaptive:1.3.0-alpha02`.

This library provides `currentWindowAdaptiveInfo()` which IS available in common code and returns `WindowAdaptiveInfo` containing `WindowSizeClass`. The `WindowSizeClass` type from this library has `.isWidthCompact`, `.isWidthMedium`, `.isWidthExpanded` boolean properties usable in common code without the companion object resolution issue.

Add to `libs.versions.toml`:
```toml
[versions]
material3Adaptive = "1.3.0-alpha02"

[libraries]
material3-adaptive = { module = "org.jetbrains.compose.material3.adaptive:adaptive", version.ref = "material3Adaptive" }
```

Add to `build.gradle.kts` (`commonMain.dependencies`):
```kotlin
implementation(libs.material3.adaptive)
```

**Remove** the previously added `material3-windowSizeClass` entry from both files — it is no longer needed.

Replace all `WindowWidthSizeClass` parameter types and usages with a simple sealed class or enum defined in the project:

```kotlin
// ui/util/WindowSize.kt
enum class WindowSize { Compact, Medium, Expanded }

// ui/util/WindowSizeUtils.kt
@Composable
fun rememberWindowSize(): WindowSize {
    val info = currentWindowAdaptiveInfo()
    return when {
        info.windowSizeClass.isWidthCompact -> WindowSize.Compact
        info.windowSizeClass.isWidthMedium  -> WindowSize.Medium
        else                                -> WindowSize.Expanded
    }
}
```

This eliminates all dependency on `androidx.compose.material3.windowsizeclass` in commonMain entirely. All screen composables accept `windowSize: WindowSize` instead of `WindowWidthSizeClass`.

### Decision 2: Library Screen — Single `LibraryVM` with filter toggle ✅

Create a new `LibraryVM` that exposes both "all jokes" and "favorites" flows, toggled by a `LibraryFilter` enum (`All`, `Favorites`). The old `FavoritesVM` and `HistoryVM` are removed.

```kotlin
enum class LibraryFilter { All, Favorites }

data class LibraryUiState(
    val jokes: List<IJoke> = emptyList(),
    val activeFilter: LibraryFilter = LibraryFilter.All,
    val isLoading: Boolean = true,
)
```

### Decision 3: Navigation — Replace HorizontalPager with manual `when` block ✅

`MainScreen` renders content based on a `selectedDestination` state. No pager. Navigation chrome (bottom bar or sidebar) is conditionally rendered based on `WindowSize`.

### Decision 4: Stats Data — Real for counts, deferred for streak ✅

- `getJokeCount(): Flow<Int>` — added to `JokeDao`
- `getFavoriteCount(): Flow<Int>` — added to `JokeDao`
- Day streak: shows `—` placeholder

### Decision 5: Filter System — Reuse `PrefsVM`, adaptive UI surface ✅

`PrefsVM`, `PrefsUiState`, `PrefsUiEvent` moved to `ui/filter/` package (unchanged logic). `FilterBottomSheet` created for mobile. `FilterPopover` and `FilterHost` still pending.

### Decision 6: Recent Jokes Section — DEFERRED ✅

Not included in initial implementation.

### Decision 7: Theme — Custom dark + light, dynamic theme retained ✅

`Color.kt`, `Type.kt`, `Theme.kt` all rewritten. Fraunces/DM Sans use `FontFamily.Serif` / `FontFamily.Default` as placeholders until font files are bundled.

### Decision 8: Filter Popover — Custom `Popup` ✅

Still planned. Not yet implemented — will be done after the window size blocker is resolved.

### Decision 9: `kotlinx.datetime` for timestamp formatting ✅

`LibraryJokeCard` formats `joke.timestamp` (epoch millis) into a human-readable string. `kotlinx-datetime` is already a transitive dependency via the project — but must be explicitly imported as `kotlinx.datetime.Instant`, `kotlinx.datetime.TimeZone`, `kotlinx.datetime.toLocalDateTime()`. The compile error confirms it is **not** yet in the explicit `commonMain` dependencies of `jestComposeApp`. Must be added to `build.gradle.kts`.

Add to `commonMain.dependencies`:
```kotlin
implementation(libs.kotlinx.datetime)
```

Verify `kotlinx-datetime` is present in `libs.versions.toml` (it likely is via `:core:commons`).

---

## 3. Implementation Plan

### Phase 0: Foundation ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 0.1 | Add font files | ⏭ Skipped | Using `FontFamily.Serif` / `FontFamily.Default` as placeholders |
| 0.2 | Add window size dependency | ✅ Done | `material3-windowSizeClass` removed; `material3-adaptive:1.3.0-alpha02` added to both `libs.versions.toml` and `build.gradle.kts` |
| 0.3 | Rewrite `Color.kt` | ✅ Done | All dark + light tokens defined, `categoryColor()` helper added |
| 0.4 | Rewrite `Type.kt` | ✅ Done | Full typography scale with Fraunces/DM Sans placeholders |
| 0.5 | Rewrite `Theme.kt` | ✅ Done | `JestDarkColorScheme` + `JestLightColorScheme`, uses `JestTypography` |

### Phase 1: Navigation & Layout Shell ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 1.1 | Create `Destination` sealed class | ✅ Done | `ui/navigation/Destination.kt` — Home, Library, Settings with icons |
| 1.2 | Create `Sidebar` composable | ✅ Done | `ui/navigation/Sidebar.kt` — branding + nav items, right-border divider |
| 1.3 | Create new `BottomBar` composable | ✅ Done | `ui/navigation/BottomBar.kt` — dark floating 3-tab bar |
| 1.4 | Rewrite `MainScreen.kt` | ✅ Done | Adaptive shell — uses `rememberWindowSize()` with `WindowSize` enum |
| 1.5 | Update `Route.kt` and `JestApp.kt` | ✅ Done | `Dashboard` is now a `data object`, `initialTab` removed |
| 1.6 | Delete old `bottombar/` package | ✅ Done | `ui/bottombar/` deleted |

### Phase 2: Reusable Components ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 2.1 | Create `FilterChip` composable | ✅ Done | `ui/component/FilterChip.kt` — icon + label + chevron, active/inactive states |
| 2.2 | Create `StatCard` composable | ✅ Done | `ui/component/StatCard.kt` — colored value + muted label |
| 2.3 | Create `HeroJokeCard` composable | ✅ Done | `ui/common/HeroJokeCard.kt` — full design match, shimmer loading state |
| 2.4 | Create `LibraryJokeCard` composable | ✅ Done | `ui/common/LibraryJokeCard.kt` — needs `kotlinx.datetime` dep (see Decision 9) |
| 2.5–2.7 | Create settings components | ✅ Done | `ui/settings/SettingsComponents.kt` — Toggle, Link, Divider rows |
| 2.8 | Delete old `JokeCard.kt` | ⏳ Pending | Delete in Phase 7 after confirming no remaining references |

### Phase 3: Filter System ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 3.1 | Create `FilterBottomSheet` | ✅ Done | `ui/filter/FilterBottomSheet.kt` — `skipPartialExpansion` removed, uses plain `rememberModalBottomSheetState()` |
| 3.2 | Create `FilterPopover` | ✅ Done | `ui/filter/FilterPopover.kt` — `Popup`-based 320dp card for tablet/desktop |
| 3.3 | Create `FilterHost` | ✅ Done | `ui/filter/FilterHost.kt` — delegates to sheet (Compact) or popover (Medium/Expanded) |
| 3.4 | Move `PrefsVM` etc. to `ui/filter/` | ✅ Done | New copies in `ui/filter/`; old `ui/tune/` deleted |
| 3.5 | Delete old `PrefsBottomSheet.kt` | ✅ Done | `ui/tune/` directory deleted entirely |

**Known fix needed in `FilterBottomSheet.kt`:** Remove `skipPartialExpansion` from `rememberModalBottomSheetState()` — the CMP version of this API doesn't have that parameter. Use plain `rememberModalBottomSheetState()`.

### Phase 4: Home Screen ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 4.1 | Add DAO queries | ✅ Done | `getJokeCount()`, `getFavoriteCount()` added to `JokeDao.kt` |
| 4.2 | Add repository methods | ✅ Done | Delegates to DAO, returns `Flow<Int>` |
| 4.3 | Rewrite `HomeUiState` | ✅ Done | Flat `data class` with `joke`, `isLoading`, `shouldAnimateJoke`, `jokeCount`, `favoriteCount` |
| 4.4 | Update `HomeUiEvent` | ✅ Done | Added `OpenFilter`, `CloseFilter` events |
| 4.5 | Rewrite `HomeVM` | ✅ Done | Separate load/observe functions, count flows wired |
| 4.6 | Rewrite `Home.kt` | ✅ Done | Filter row → hero card → stats row; uses `WindowSize`; delegates to `FilterHost` |

### Phase 5: Library Screen ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 5.1 | Create `LibraryUiState` | ✅ Done | With `LibraryFilter` enum |
| 5.2 | Create `LibraryUiEvent` | ✅ Done | `FilterChanged`, `FavoriteToggled` |
| 5.3 | Create `LibraryVM` | ✅ Done | `flatMapLatest` on filter, combined state flow |
| 5.4 | Create `Library.kt` | ✅ Done | `LazyColumn` (mobile) / `LazyVerticalGrid` (tablet+); uses `WindowSize`; delegates to `FilterHost` |
| 5.5 | Delete old `ui/list/` package | ✅ Done | `ui/list/` directory deleted |

### Phase 6: Settings Screen ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 6.1 | Rewrite `Settings.kt` | ✅ Done | Mobile / tablet / desktop column layouts; uses `WindowSize` |
| 6.2 | Minimal VM changes | ✅ Done | No logic changes needed |

### Phase 7: Cleanup & Polish ✅ COMPLETE

| # | Task | Status | Notes |
|---|------|--------|-------|
| 7.1 | Update `JestModule.kt` | ✅ Done | `FavoritesVM`/`HistoryVM` removed, `LibraryVM` added, `PrefsVM` import updated |
| 7.2 | Update `@Preview` composables | ✅ Done | All new files use `androidx.compose.ui.tooling.preview.Preview` |
| 7.3 | Delete `ui/bottombar/` | ✅ Done | Directory deleted |
| 7.4 | Delete `ui/tune/` | ✅ Done | Directory deleted |
| 7.5 | Delete `ui/list/` | ✅ Done | Directory deleted |
| 7.6 | Delete `ui/common/JokeCard.kt` | ✅ Done | No remaining references confirmed; file deleted |
| 7.7 | Verify Android build | ✅ Done | `JestActivity.kt` fixed to use `JestDarkColorScheme`/`JestLightColorScheme`; `compileDebugKotlinAndroid` passes |
| 7.8 | Update Jest context doc | ✅ Done | Reflects new packages, screens, VM names, adaptive library, filter system |

---

## 4. File Change Summary

### New Files ✅ Created
- `ui/navigation/Destination.kt`
- `ui/navigation/Sidebar.kt`
- `ui/navigation/BottomBar.kt`
- `ui/component/FilterChip.kt`
- `ui/component/StatCard.kt`
- `ui/common/HeroJokeCard.kt`
- `ui/common/LibraryJokeCard.kt`
- `ui/settings/SettingsComponents.kt`
- `ui/library/Library.kt`
- `ui/library/LibraryVM.kt`
- `ui/library/LibraryUiState.kt`
- `ui/library/LibraryUiEvent.kt`
- `ui/filter/PrefsVM.kt`
- `ui/filter/PrefsUiState.kt`
- `ui/filter/PrefsUiEvent.kt`
- `ui/filter/FilterBottomSheet.kt`
- `ui/util/WindowSizeUtils.kt` *(will be rewritten for `adaptive` library)*

### New Files ✅ Created (remaining)
- `ui/filter/FilterPopover.kt` ✅ — `Popup`-based 320dp card for tablet/desktop
- `ui/filter/FilterHost.kt` ✅ — adaptive chooser between sheet and popover
- `ui/util/WindowSize.kt` ✅ — `enum class WindowSize { Compact, Medium, Expanded }`

### New Files ⏭ Skipped (deferred)
- `composeResources/font/fraunces_*.ttf` — system font placeholders in use
- `composeResources/font/dm_sans_*.ttf` — system font placeholders in use

### Modified Files ✅ Done
- `libs.versions.toml` — `material3-windowSizeClass` removed; `material3Adaptive = "1.3.0-alpha02"` + `material3-adaptive` library entry added
- `build.gradle.kts` — swapped to `libs.material3.adaptive`; added `libs.kotlinx.datetime`
- `ui/theme/Color.kt` — full rewrite
- `ui/theme/Type.kt` — full rewrite
- `ui/theme/Theme.kt` — new color schemes + `JestTypography`
- `ui/MainScreen.kt` — full rewrite (adaptive shell)
- `ui/home/Home.kt` — full rewrite
- `ui/home/HomeUiState.kt` — flat data class
- `ui/home/HomeUiEvent.kt` — filter events added
- `ui/home/HomeVM.kt` — rewritten, count flows wired
- `ui/settings/Settings.kt` — full rewrite (responsive columns)
- `ui/navigation/Route.kt` — `Dashboard` is now `data object`
- `JestApp.kt` — `initialTab` removed
- `di/JestModule.kt` — VMs swapped
- `database/dao/JokeDao.kt` — count queries added
- `repository/JokeRepository.kt` — count methods added
- `androidMain/JestActivity.kt` — system bar tinting updated to use `JestDarkColorScheme`/`JestLightColorScheme`
- `ui/theme/Theme.kt` — `JestDarkColorScheme`/`JestLightColorScheme` changed from `private` to `internal`

### Files Fixed ✅
- `ui/MainScreen.kt` — `WindowWidthSizeClass` replaced with `WindowSize`; `@Preview` import fixed
- `ui/home/Home.kt` — `WindowWidthSizeClass` replaced with `WindowSize`; `@Preview` import fixed; uses `FilterHost`
- `ui/library/Library.kt` — `WindowWidthSizeClass` replaced with `WindowSize`; `@Preview` import fixed; uses `FilterHost`
- `ui/settings/Settings.kt` — `WindowWidthSizeClass` replaced with `WindowSize`; `@Preview` import fixed
- `ui/util/WindowSizeUtils.kt` — rewritten to use `currentWindowAdaptiveInfo()` + `isWidthAtLeastBreakpoint()`
- `ui/filter/FilterBottomSheet.kt` — `skipPartialExpansion` removed; `@Preview` import fixed
- `ui/common/LibraryJokeCard.kt` — `@Preview` import fixed (`kotlinx.datetime` dep added to build file)
- `ui/common/HeroJokeCard.kt`, `ui/component/FilterChip.kt`, `ui/component/StatCard.kt`, `ui/navigation/BottomBar.kt`, `ui/navigation/Sidebar.kt`, `ui/settings/SettingsComponents.kt` — `@Preview` import fixed

### Moved Files ✅ Done (originals not yet deleted)
- `ui/tune/PrefsVM.kt` → `ui/filter/PrefsVM.kt`
- `ui/tune/PrefsUiState.kt` → `ui/filter/PrefsUiState.kt`
- `ui/tune/PrefsUiEvent.kt` → `ui/filter/PrefsUiEvent.kt`

### Deleted Files ✅ Done (Phase 7)
- `ui/bottombar/` (entire directory)
- `ui/list/` (entire directory)
- `ui/tune/` (entire directory)
- `ui/common/JokeCard.kt`

---

## 5. Status — COMPLETE ✅

All phases are done. Both `compileKotlinJvm` and `compileDebugKotlinAndroid` pass.

### Key implementation notes for future reference

- **`WindowSizeClass` API**: `currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)` — use this pattern, not `windowWidthSizeClass` (deprecated) and not `isWidthCompact`/`isWidthMedium` (don't exist in this version).
- **`@Preview` import**: Must be `androidx.compose.ui.tooling.preview.Preview` in all `commonMain` files — `org.jetbrains.compose.ui.tooling.preview.Preview` resolves but doesn't render in Android Studio.
- **`JestDarkColorScheme`/`JestLightColorScheme`**: `internal` visibility so `JestActivity` (androidMain) can access them for system bar tinting fallback.
- **`FilterHost`**: Single call-site API for filter UI — pass `windowSize` + `onDismiss`, it picks sheet vs. popover.

---

## 6. Dependency Graph — RESOLVED ✅

All blockers resolved. Build passes on JVM and Android targets.

---

## 7. Deferred Items (Follow-up Work)

| Item | Description | Reason |
|------|-------------|--------|
| Fraunces + DM Sans font files | Bundle `.ttf` files in `composeResources/font/` and update `Type.kt` | Skipped for initial implementation — system fonts used as placeholders |
| Recent Jokes section on Home | Desktop right panel + mobile section showing recent jokes | Deferred per Decision 6 |
| Day Streak tracking | Needs daily usage tracking mechanism (new DB table or DataStore) | Deferred — shows `—` placeholder in StatCard |
| Light theme visual QA | Design file is dark-only; light tokens created programmatically | May need visual polish after initial implementation |
| Landscape mode on mobile | Not in design file | May need attention post-revamp |

---

## Related Context

- [Jest Context](./context.md) — Current architecture and module structure
- [Coding Conventions](../../coding-conventions.md) — Compose patterns to follow
- [Commons Module](../../core/commons/context.md) — Shared utilities
- [Build System](../../build-system.md) — Convention plugins, version catalog